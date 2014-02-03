package org.osmdroid.tileprovider;

import android.util.DisplayMetrics;
import org.osmdroid.tileprovider.modules.*;
import org.osmdroid.tileprovider.tilesource.ITileSource;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.tileprovider.util.SimpleRegisterReceiver;

import android.content.Context;
import org.osmdroid.views.MapView;

/**
 * This top-level tile provider implements a basic tile request chain which includes a
 * {@link MapTileFilesystemProvider} (a file-system cache), a {@link MapTileFileArchiveProvider}
 * (archive provider), and a {@link MapTileDownloader} (downloads map tiles via tile source).
 *
 * @author Marc Kurtz
 */
public class MapTileProviderBasic extends MapTileProviderArray implements IMapTileProviderCallback {
    Context context;
    // private static final Logger logger = LoggerFactory.getLogger(MapTileProviderBasic.class);

    /**
     * Creates a {@link MapTileProviderBasic}.
     */
    public MapTileProviderBasic(final Context pContext) {
        this(pContext, TileSourceFactory.DEFAULT_TILE_SOURCE, null);
    }

    /**
     * Creates a {@link MapTileProviderBasic}.
     */
    public MapTileProviderBasic(final Context pContext, final ITileSource pTileSource, MapView mapView) {
        this(new SimpleRegisterReceiver(pContext), new NetworkAvailabilityCheck(pContext),
                pTileSource, pContext, mapView);
    }

    /**
     * Creates a {@link MapTileProviderBasic}.
     */
    public MapTileProviderBasic(final IRegisterReceiver pRegisterReceiver,
                                final INetworkAvailabilityCheck aNetworkAvailablityCheck, final ITileSource pTileSource, Context context, MapView mapView) {
        super(pTileSource, pRegisterReceiver);
        this.context = context;
        final TileWriter tileWriter = new TileWriter();

        final MapTileFilesystemProvider fileSystemProvider = new MapTileFilesystemProvider(
                pRegisterReceiver, pTileSource);
        mTileProviderList.add(fileSystemProvider);
//
//        final MapTileFileArchiveProvider archiveProvider = new MapTileFileArchiveProvider(
//                pRegisterReceiver, pTileSource);
//        mTileProviderList.add(archiveProvider);

        final MapTileDownloader downloaderProvider = new MapTileDownloader(pTileSource, tileWriter,
                aNetworkAvailablityCheck, mapView);
        if(isHighDensity()){
            downloaderProvider.setHighDensity(true);
        }
        for(MapTileModuleProviderBase provider: mTileProviderList){
            if(provider.getClass().isInstance(MapTileDownloader.class)){
                mTileProviderList.remove(provider);
            }
        }
        mTileProviderList.add(downloaderProvider);
    }
    public boolean isHighDensity(){
        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        if (metrics.densityDpi>300){
            return true;
        }
        return false;
    }
}
