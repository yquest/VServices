package pt.fabm;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Promise;
import io.vertx.core.buffer.Buffer;

import java.util.function.Function;
import java.util.function.Supplier;

public class CachedReader<T> implements Handler<T>, AsyncResult<T> {
    private final long delay;
    private final Function<Buffer, T> fromBuffer;
    private final Function<T, Buffer> toBuffer;
    private final Function<Buffer, Promise<Void>> asyncUpdater;
    private final Supplier<Promise<Buffer>> asyncReader;
    private Future<T> cache;
    private long start;

    public CachedReader(
            long delay,
            Function<Buffer, T> fromBuffer,
            Function<T, Buffer> toBuffer,
            Function<Buffer, Promise<Void>> asyncUpdater,
            Supplier<Promise<Buffer>> asyncReader
    ) {
        this.delay = delay;
        this.fromBuffer = fromBuffer;
        this.toBuffer = toBuffer;
        this.asyncUpdater = asyncUpdater;
        this.asyncReader = asyncReader;
        reset();
    }

    public Future<T> getCache() {
        synchronized (this) {
            long now = System.currentTimeMillis();
            if (now > start + delay) {
                start = now;
                Promise<Buffer> reader = asyncReader.get();
                cache = reader.future().map(fromBuffer);
            }
            return cache;
        }
    }

    public void reset() {
        long now = System.currentTimeMillis();
        start = now - delay - 1000;
    }

    /**
     * triggered on update
     *
     * @param event updateEvent
     */
    @Override
    public void handle(T event) {
        synchronized (this) {
            cache = Future.succeededFuture(event);
        }
    }

    @Override
    public T result() {
        return cache.result();
    }

    @Override
    public Throwable cause() {
        return cache.cause();
    }

    @Override
    public boolean succeeded() {
        return cache.succeeded();
    }

    @Override
    public boolean failed() {
        return cache.failed();
    }

    public Future<Void> updateAsync(T entry) {
        Buffer buffer = toBuffer.apply(entry);
        return asyncUpdater.apply(buffer).future().map(ignore -> {
            synchronized (this) {
                cache = Future.succeededFuture(entry);
            }
            return null;
        });
    }
}
