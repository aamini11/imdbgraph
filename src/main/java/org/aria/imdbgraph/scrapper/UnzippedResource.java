package org.aria.imdbgraph.scrapper;

import org.springframework.core.io.Resource;
import org.springframework.lang.Nullable;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.nio.channels.ReadableByteChannel;
import java.util.zip.GZIPInputStream;

/**
 * The flat files IMDB provides are zipped by default and need to be passed through
 * java's GZIPInputStream before being read.
 */
final class UnzippedResource implements Resource {

    private final Resource delegate;

    UnzippedResource(Resource resource) {
        this.delegate = resource;
    }

    @Override
    public InputStream getInputStream() throws IOException {
        return new GZIPInputStream(delegate.getInputStream());
    }

    @Override
    public boolean exists() {
        return delegate.exists();
    }

    @Override
    public boolean isReadable() {
        return delegate.isReadable();
    }

    @Override
    public boolean isOpen() {
        return delegate.isOpen();
    }

    @Override
    public boolean isFile() {
        return delegate.isFile();
    }

    @Override
    public URL getURL() throws IOException {
        return delegate.getURL();
    }

    @Override
    public URI getURI() throws IOException {
        return delegate.getURI();
    }

    @Override
    public File getFile() throws IOException {
        return delegate.getFile();
    }

    @Override
    public ReadableByteChannel readableChannel() throws IOException {
        return delegate.readableChannel();
    }

    @Override
    public long contentLength() throws IOException {
        return delegate.contentLength();
    }

    @Override
    public long lastModified() throws IOException {
        return delegate.lastModified();
    }

    @Override
    public Resource createRelative(String s) throws IOException {
        return delegate.createRelative(s);
    }

    @Override
    @Nullable
    public String getFilename() {
        return delegate.getFilename();
    }

    @Override
    public String getDescription() {
        return delegate.getDescription();
    }
}
