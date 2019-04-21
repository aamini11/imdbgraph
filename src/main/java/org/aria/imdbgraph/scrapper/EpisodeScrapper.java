package org.aria.imdbgraph.scrapper;

import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.batch.item.database.builder.JdbcBatchItemWriterBuilder;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.core.io.Resource;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;

import javax.sql.DataSource;
import java.util.function.Function;

import static org.aria.imdbgraph.scrapper.JobConfig.CHUNK_SIZE;

/**
 * Static utility class to create the episode scrapping step to be used in the job configuration.
 */
class EpisodeScrapper {

    private EpisodeScrapper() {}

    /**
     * POJO to represent a record in the Episode file.
     */
    private static final class EpisodeRecord {
        final String episodeId;
        final String showId;
        final int season;
        final int episode;

        EpisodeRecord(String line) {
            String[] fields = line.split("\t");
            episodeId = fields[0];
            showId = fields[1];
            season = (fields[2].equals("\\N")) ? -1 : Integer.parseInt(fields[2]);
            episode = (fields[3].equals("\\N")) ? -1 : Integer.parseInt(fields[3]);
        }
    }

    /**
     * Factory method to create the scrapping step that will read in episode data. The step will get its input
     * from a flat file that IMDB makes available on their website. And all that episode information is then promptly
     * written to the database.
     *
     * @param stepBuilder Requires a stepBuilder to create the step object. (Normally this builder is provided by
     *                    spring and autowired as a dependency in the job configuration file that calls this method.
     * @param input A generic resource that the reader will use to get its input from
     * @param dataSource The datasource the item writer will use to perform SQL updates.
     * @return A fully configured step to be used by the job config.
     */
    static Step createEpisodeScrapper(StepBuilderFactory stepBuilder, Resource input, DataSource dataSource) {
        return stepBuilder.get("updateEpisodes")
                .<EpisodeRecord, EpisodeRecord>chunk(CHUNK_SIZE)
                .reader(createReader(input))
                .processor((Function<EpisodeRecord, EpisodeRecord>) record -> {
                    if (record.episode != -1 && record.season != -1) return record;
                    else return null;
                })
                .writer(createWriter(dataSource))
                .build();
    }

    private static FlatFileItemReader<EpisodeRecord> createReader(Resource resource) {
        return new FlatFileItemReaderBuilder<EpisodeRecord>()
                .name("imdbEpisodeReader")
                .resource(resource)
                .linesToSkip(1)
                .lineMapper((line, lineNumber) -> new EpisodeRecord(line))
                .build();
    }

    private static JdbcBatchItemWriter<EpisodeRecord> createWriter(DataSource dataSource) {
        //language=SQL
        final String updateSql = "" +
                "INSERT INTO imdb.episode(show_id, episode_id, season, episode)\n" +
                "VALUES (:showId, :episodeId, :season, :episode)\n" +
                "ON CONFLICT (show_id, episode_id) DO NOTHING;";

        var writer = new JdbcBatchItemWriterBuilder<EpisodeRecord>()
                .sql(updateSql)
                .dataSource(dataSource)
                .itemSqlParameterSourceProvider(record -> new MapSqlParameterSource()
                        .addValue("showId", record.showId)
                        .addValue("episodeId", record.episodeId)
                        .addValue("season", record.season)
                        .addValue("episode", record.episode))
                .assertUpdates(false)
                .build();
        writer.afterPropertiesSet();
        return writer;
    }
}
