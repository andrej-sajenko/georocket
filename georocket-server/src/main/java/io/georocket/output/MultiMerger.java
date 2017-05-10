package io.georocket.output;

import io.georocket.output.geojson.GeoJsonMerger;
import io.georocket.output.xml.XMLMerger;
import io.georocket.storage.ChunkMeta;
import io.georocket.storage.ChunkReadStream;
import io.georocket.storage.GeoJsonChunkMeta;
import io.georocket.storage.XMLChunkMeta;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.streams.WriteStream;
import rx.Completable;

/**
 * <p>A merger that either delegates to {@link XMLMerger} or
 * {@link GeoJsonMerger} depending on the types of the chunks to merge.</p>
 * <p>For the time being the merger can only merge chunks of the same type.
 * In the future it may create an archive (e.g. a ZIP or a TAR file) containing
 * chunks of mixed types.</p>
 * @author Michel Kraemer
 */
public class MultiMerger implements Merger<ChunkMeta> {
  private XMLMerger xmlMerger;
  private GeoJsonMerger geoJsonMerger;
  
  private Completable ensureMerger(ChunkMeta meta) {
    if (meta instanceof XMLChunkMeta) {
      if (xmlMerger == null) {
        if (geoJsonMerger != null) {
          return Completable.error(new IllegalStateException("Cannot merge "
            + "XML chunk into a GeoJSON document."));
        }
        xmlMerger = new XMLMerger();
      }
      return Completable.complete();
    } else if (meta instanceof GeoJsonChunkMeta) {
      if (geoJsonMerger == null) {
        if (xmlMerger != null) {
          return Completable.error(new IllegalStateException("Cannot merge "
            + "GeoJSON chunk into an XML document."));
        }
        geoJsonMerger = new GeoJsonMerger();
      }
      return Completable.complete();
    }
    return Completable.error(new IllegalStateException("Cannot merge "
      + "chunk of type " + meta.getMimeType()));
  }
  
  @Override
  public Completable init(ChunkMeta meta) {
    return ensureMerger(meta)
      .toObservable()
      .flatMapCompletable(v -> {
        if (meta instanceof XMLChunkMeta) {
          return xmlMerger.init((XMLChunkMeta) meta);
        }
        return geoJsonMerger.init((GeoJsonChunkMeta)meta);
      }).toCompletable();
  }

  @Override
  public Completable merge(ChunkReadStream chunk, ChunkMeta meta,
      WriteStream<Buffer> out) {
    return ensureMerger(meta)
      .toObservable()
      .flatMapCompletable(v -> {
        if (meta instanceof XMLChunkMeta) {
          return xmlMerger.merge(chunk, (XMLChunkMeta)meta, out);
        }
        return geoJsonMerger.merge(chunk, (GeoJsonChunkMeta)meta, out);
      }).toCompletable();
  }

  @Override
  public void finish(WriteStream<Buffer> out) {
    if (xmlMerger != null) {
      xmlMerger.finish(out);
    }
    if (geoJsonMerger != null) {
      geoJsonMerger.finish(out);
    }
  }
}
