package io.georocket.storage;

import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.rx.java.ObservableFuture;
import io.vertx.rx.java.RxHelper;
import rx.Completable;
import rx.Single;

/**
 * Wraps around {@link Store} and adds methods to be used with RxJava
 * @author Michel Kraemer
 */
public class RxStore implements Store {
  private final Store delegate;
  
  /**
   * Create a new rx-ified store
   * @param delegate the actual store to delegate to
   */
  public RxStore(Store delegate) {
    this.delegate = delegate;
  }
  
  /**
   * @return the actual non-rx-ified store
   */
  public Store getDelegate() {
    return delegate;
  }
  
  @Override
  public void add(String chunk, ChunkMeta chunkMeta, String path,
      IndexMeta indexMeta, Handler<AsyncResult<Void>> handler) {
    delegate.add(chunk, chunkMeta, path, indexMeta, handler);
  }
  
  /**
   * Observable version of {@link #add(String, ChunkMeta, String, IndexMeta, Handler)}
   * @param chunk the chunk to add
   * @param chunkMeta the chunk's metadata
   * @param path the path where the chunk should be stored (may be null)
   * @param indexMeta metadata affecting the way the chunk will be indexed
   * @return completes when the operation has completed
   */
  public Completable rxAdd(String chunk, ChunkMeta chunkMeta,
      String path, IndexMeta indexMeta) {
    ObservableFuture<Void> o = RxHelper.observableFuture();
    add(chunk, chunkMeta, path, indexMeta, o.toHandler());
    return o.toCompletable();
  }

  @Override
  public void getOne(String path, Handler<AsyncResult<ChunkReadStream>> handler) {
    delegate.getOne(path, handler);
  }
  
  /**
   * Observable version of {@link #getOne(String, Handler)}
   * @param path the absolute path to the chunk
   * @return emit a read stream that can be used to
   * get the chunk's contents
   */
  public Single<ChunkReadStream> rxGetOne(String path) {
    ObservableFuture<ChunkReadStream> o = RxHelper.observableFuture();
    getOne(path, o.toHandler());
    return o.toSingle();
  }

  @Override
  public void delete(String search, String path, Handler<AsyncResult<Void>> handler) {
    delegate.delete(search, path, handler);
  }
  
  /**
   * Observable version of {@link #delete(String, String, Handler)}
   * @param search the search query
   * @param path the path where to search for the chunks (may be null)
   * @return complete when the operation has completed
   */
  public Completable rxDelete(String search, String path) {
    ObservableFuture<Void> o = RxHelper.observableFuture();
    delete(search, path, o.toHandler());
    return o.toCompletable();
  }

  @Override
  public void get(String search, String path, Handler<AsyncResult<StoreCursor>> handler) {
    delegate.get(search, path, handler);
  }
  
  /**
   * Observable version of {@link #get(String, String, Handler)}
   * @param search the search query
   * @param path the path where to search for the chunks (may be null)
   * @return emits a cursor that can be used to iterate
   * over all matched chunks
   */
  public Single<StoreCursor> rxGet(String search, String path) {
    ObservableFuture<StoreCursor> o = RxHelper.observableFuture();
    get(search, path, o.toHandler());
    return o.toSingle();
  }
}
