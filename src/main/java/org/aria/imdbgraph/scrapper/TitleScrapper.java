package org.aria.imdbgraph.scrapper;

import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.batch.item.database.builder.JdbcBatchItemWriterBuilder;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.core.io.Resource;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;

import javax.sql.DataSource;

import static org.aria.imdbgraph.scrapper.JobConfig.CHUNK_SIZE;

/**
 * Static utility class to create the title scrapping step which is used in the job configuration.
 */
class TitleScrapper {

    private TitleScrapper() {}

    private static final class TitleRecord {
        final String imdbId;
        final String titleType;
        final String primaryTitle;
        final String originalTitle;
        final String startYear;
        final String endYear;

        TitleRecord(String line) {
            String[] fields = line.split("\t");
            imdbId = fields[0];
            titleType = fields[1];
            primaryTitle = fields[2];
            originalTitle = fields[3];
            startYear = (fields[5].equals("\\N")) ? null : fields[5];
            endYear = (fields[6].equals("\\N")) ? null : fields[6];
        }
    }

    static Step createTitleScrapper(StepBuilderFactory stepBuilder, Resource input, DataSource dataSource) {
        return stepBuilder.get("updateTitles")
                .<TitleRecord, TitleRecord>chunk(CHUNK_SIZE)
                .reader(createReader(input))
                .processor((ItemProcessor<TitleRecord, TitleRecord>) record -> {
                    if (record.titleType.equals("tvEpisode") || record.titleType.equals("tvSeries")) return record;
                    else return null;
                })
                .writer(sqlTitleWriter(dataSource))
                .build();
    }

    private static FlatFileItemReader<TitleRecord> createReader(Resource resourceToRead) {
        return new FlatFileItemReaderBuilder<TitleRecord>()
                .name("imdbEpisodeReader")
                .resource(resourceToRead)
                .linesToSkip(1)
                .lineMapper((line, lineNum) -> new TitleRecord(line))
                .build();
    }

    private static JdbcBatchItemWriter<TitleRecord> sqlTitleWriter(DataSource dataSource) {
        //language=SQL
        final String updateShowTitleSql =
                "INSERT INTO imdb.title(imdb_id, primary_title, title_type, start_year, end_year)\n" +
                "VALUES (:imdbId, :primaryTitle, :titleType, :startYear, :endYear)\n" +
                "ON CONFLICT (imdb_id) DO UPDATE\n" +
                "SET \n" +
                "  end_year = :endYear;";
        JdbcBatchItemWriter<TitleRecord> showTitleWriter = new JdbcBatchItemWriterBuilder<TitleRecord>()
                .sql(updateShowTitleSql)
                .dataSource(dataSource)
                .itemSqlParameterSourceProvider(record -> new MapSqlParameterSource()
                        .addValue("imdbId", record.imdbId)
                        .addValue("primaryTitle", record.primaryTitle)
                        .addValue("titleType", record.titleType)
                        .addValue("startYear", record.startYear)
                        .addValue("endYear", record.endYear))
                .build();
        showTitleWriter.afterPropertiesSet();
        return showTitleWriter;
    }
}