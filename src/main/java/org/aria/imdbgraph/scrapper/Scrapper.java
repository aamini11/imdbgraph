package org.aria.imdbgraph.scrapper;

import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.item.file.ResourceAwareItemReaderItemStream;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.core.io.FileSystemResource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcOperations;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import javax.sql.DataSource;
import java.nio.file.Path;
import java.util.List;
import java.util.function.Function;

/**
 * Base class used to implement all the classes that will be responsible
 * for scrapping data from Flat files provided by IMDB.
 *
 * @param <T> The type representing each record of the file being parsed
 */
abstract class Scrapper<T> {

    private static final int CHUNK_SIZE = 100;

    private final StepBuilderFactory stepBuilderFactory;
    final NamedParameterJdbcOperations jdbc;

    /**
     * Constructor to setup a scrapper responsible for scrapping an IMDB file.
     * Constructor is package private to prevent subclassing from outside.
     *
     * @param stepBuilderFactory A stepBuilder provided by spring. Each
     *                           implementation will return a step responsible
     *                           for parsing the file.
     * @param dataSource         The datasource the item writer will use to
     *                           perform SQL updates.
     */
    Scrapper(StepBuilderFactory stepBuilderFactory,
             DataSource dataSource) {
        this.stepBuilderFactory = stepBuilderFactory;
        this.jdbc = new NamedParameterJdbcTemplate(dataSource);
    }

    /**
     * Configures a step in the daily job that scraps files from IMDB
     *
     * @param fileLocation The path to the file to be scrapped
     * @return {@link Step} object to be used by spring batch.
     */
    Step createStep(Path fileLocation) {
        return stepBuilderFactory.get(this.getClass().getSimpleName())
                .<T, T>chunk(CHUNK_SIZE)
                .reader(createReader(fileLocation, this::mapLine))
                .writer(this::saveRecords)
                .build();
    }

    /**
     * Save method responsible for updating the database with record data.
     *
     * @param records The record POJOs which were read from the file and are
     *                being used to update the database
     */
    abstract void saveRecords(List<? extends T> records);

    /**
     * Method used to map each line from the file into a record POJO.
     *
     * @param line from the file being parsed
     * @return The line converted to a record POJO
     */
    abstract T mapLine(String line);

    private ResourceAwareItemReaderItemStream<T> createReader(
            Path fileLocation,
            Function<String, T> lineMapper) {
        return new FlatFileItemReaderBuilder<T>()
                .name(this.getClass().getSimpleName() + "Reader")
                .resource(new FileSystemResource(fileLocation))
                .linesToSkip(1)
                .lineMapper((line, lineNum) -> lineMapper.apply(line))
                .build();
    }
}
