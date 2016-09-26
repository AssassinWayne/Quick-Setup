package me.wh.common.image;

import android.content.Context;

import com.bumptech.glide.Glide;
import com.bumptech.glide.GlideBuilder;
import com.bumptech.glide.MemoryCategory;
import com.bumptech.glide.integration.okhttp3.OkHttpGlideModule;
import com.bumptech.glide.load.DecodeFormat;
import com.bumptech.glide.load.engine.bitmap_recycle.LruBitmapPool;
import com.bumptech.glide.load.engine.cache.ExternalCacheDiskCacheFactory;
import com.bumptech.glide.load.engine.cache.InternalCacheDiskCacheFactory;
import com.bumptech.glide.load.engine.cache.LruResourceCache;
import com.bumptech.glide.load.engine.cache.MemorySizeCalculator;

import me.wh.common.util.DeviceUtil;
import me.wh.common.util.SizeUtil;

public class CustomGlideModule extends OkHttpGlideModule {
    @Override
    public void applyOptions(Context context, GlideBuilder builder) {
        if (DeviceUtil.existSDCard()) {
            builder.setDiskCache(new ExternalCacheDiskCacheFactory(context, SizeUtil.SIZE_512M));
        } else {
            builder.setDiskCache(new InternalCacheDiskCacheFactory(context, SizeUtil.SIZE_256M));
        }
        MemorySizeCalculator calculator = new MemorySizeCalculator(context);
        builder.setBitmapPool(new LruBitmapPool(calculator.getBitmapPoolSize()));
        builder.setMemoryCache(new LruResourceCache(calculator.getMemoryCacheSize()));
        builder.setDecodeFormat(DecodeFormat.PREFER_ARGB_8888);
    }

    @Override
    public void registerComponents(Context context, Glide glide) {
        super.registerComponents(context, glide);
        glide.setMemoryCategory(MemoryCategory.NORMAL);
    }
}
