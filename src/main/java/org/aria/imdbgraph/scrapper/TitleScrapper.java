package org.aria.imdbgraph.scrapper;

import org.springframework.batch.core.JobInterruptedException;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.batch.item.database.builder.JdbcBatchItemWriterBuilder;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.batch.item.support.ClassifierCompositeItemWriter;
import org.springframework.core.io.Resource;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcOperations;

import java.util.function.Function;

import static org.aria.imdbgraph.scrapper.ImdbScrappingJob.CHUNK_SIZE;
import static org.aria.imdbgraph.scrapper.TitleScrapper.TitleType.TVEPISODE;
import static org.aria.imdbgraph.scrapper.TitleScrapper.TitleType.TVSERIES;

final class TitleScrapper implements Step {

    private final Step delegateStep;

    TitleScrapper(NamedParameterJdbcOperations jdbc, StepBuilderFactory stepBuilderFactory, Resource resourceToRead) {
        this.delegateStep = createStep(jdbc, stepBuilderFactory, resourceToRead);
    }

    enum TitleType {
        MOVIE,
        SHORT,
        TVSERIES,
        TVEPISODE,
        TVMOVIE,
        VIDEO,
        TVSHORT,
        TVMINISERIES,
        TVSPECIAL,
        VIDEOGAME
    }

    private static final class TitleRecord {
        final String imdbId;
        final TitleType titleType;
        final String primaryTitle;
        final String originalTitle;
        final String startYear;
        final String endYear;

        TitleRecord(String line) {
            String[] fields = line.split("\t");
            imdbId = fields[0];
            titleType = TitleType.valueOf(fields[1].toUpperCase());
            primaryTitle = fields[2];
            originalTitle = fields[3];
            startYear = (fields[5].equals("\\N")) ? null : fields[5];
            endYear = (fields[6].equals("\\N")) ? null : fields[6];
        }
    }

    private static Step createStep(NamedParameterJdbcOperations jdbc, StepBuilderFactory stepBuilderFactory, Resource resourceToRead) {
        return stepBuilderFactory.get("updateTitles")
                .<TitleRecord, TitleRecord>chunk(CHUNK_SIZE)
                .reader(createReader(resourceToRead))
                .processor((Function<TitleRecord, TitleRecord>) show -> {
                    if (show.titleType == TVSERIES || show.titleType == TVEPISODE) return show;
                    else return null;
                })
                .writer(sqlTitleWriter(jdbc))
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

    private static ClassifierCompositeItemWriter<TitleRecord> sqlTitleWriter(NamedParameterJdbcOperations jdbc) {
        //language=SQL
        final String updateShowTitleSql = "" +
                "INSERT INTO imdb.show_title(show_id, primary_title, start_year, end_year)\n" +
                "VALUES (:imdbId, :primaryTitle, :startYear, :endYear)\n" +
                "ON CONFLICT (show_id) DO UPDATE\n" +
                "SET \n" +
                "  end_year = :endYear;";
        JdbcBatchItemWriter<TitleRecord> showTitleWriter = new JdbcBatchItemWriterBuilder<TitleRecord>()
                .sql(updateShowTitleSql)
                .namedParametersJdbcTemplate(jdbc)
                .itemSqlParameterSourceProvider(record -> new MapSqlParameterSource()
                        .addValue("imdbId", record.imdbId)
                        .addValue("primaryTitle", record.primaryTitle)
                        .addValue("startYear", record.startYear)
                        .addValue("endYear", record.endYear))
                .build();
        showTitleWriter.afterPropertiesSet();

        //language=SQL
        final String updateEpisodeTitleSql = "" +
                "INSERT INTO imdb.episode_title(episode_id, primary_title)\n" +
                "VALUES (:imdbId, :primaryTitle)\n" +
                "ON CONFLICT (episode_id) DO NOTHING;";
        JdbcBatchItemWriter<TitleRecord> episodeTitleWriter = new JdbcBatchItemWriterBuilder<TitleRecord>()
                .sql(updateEpisodeTitleSql)
                .namedParametersJdbcTemplate(jdbc)
                .itemSqlParameterSourceProvider(record -> new MapSqlParameterSource()
                        .addValue("imdbId", record.imdbId)
                        .addValue("primaryTitle", record.primaryTitle))
                .assertUpdates(false)
                .build();
        episodeTitleWriter.afterPropertiesSet();

        ClassifierCompositeItemWriter<TitleRecord> writer = new ClassifierCompositeItemWriter<>();
        writer.setClassifier(titleRecord -> {
            if (titleRecord.titleType == TVEPISODE) return episodeTitleWriter;
            if (titleRecord.titleType == TVSERIES) return showTitleWriter;
            return null;
        });
        return writer;
    }

    @Override
    public String getName() {
        return delegateStep.getName();
    }

    @Override
    public boolean isAllowStartIfComplete() {
        return delegateStep.isAllowStartIfComplete();
    }

    @Override
    public int getStartLimit() {
        return delegateStep.getStartLimit();
    }

    @Override
    public void execute(StepExecution stepExecution) throws JobInterruptedException {
        delegateStep.execute(stepExecution);
    }
}