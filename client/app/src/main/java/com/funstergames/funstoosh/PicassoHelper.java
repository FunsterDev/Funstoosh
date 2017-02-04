package com.funstergames.funstoosh;

import android.content.Context;

import com.koushikdutta.ion.Ion;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.picasso.OkHttpDownloader;
import com.squareup.picasso.Picasso;

public class PicassoHelper {
    public static class PatchedOkHttpDownloader extends OkHttpDownloader {
        public PatchedOkHttpDownloader(Context context) {
            super(context);
        }

        public OkHttpClient getClientPatch() {
            return getClient();
        }
    }

    public static Picasso getPicasso(Context context) {
        PatchedOkHttpDownloader downloader = new PatchedOkHttpDownloader(context);
        downloader.getClientPatch().setCookieHandler(Ion.getDefault(context).getCookieMiddleware().getCookieManager());
        return new Picasso.Builder(context).downloader(downloader).build();
    }
}
