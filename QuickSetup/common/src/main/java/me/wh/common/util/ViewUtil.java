package me.wh.common.util;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.media.ExifInterface;
import android.os.Build;
import android.util.DisplayMetrics;
import android.view.KeyCharacterMap;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.ListView;

import java.io.IOException;
import java.lang.reflect.Field;

public class ViewUtil {
    public static final int LIST_VIEW_COME_BACK_TUNNEL = 20;

    public static int getScreenWidth(Context context) {
        return context.getResources().getDisplayMetrics().widthPixels;
    }

    public static int getScreenHeight(Context context) {
        return context.getResources().getDisplayMetrics().heightPixels;
    }

    public static String getImageFormat(Context context) {
        int dpi = context.getResources().getDisplayMetrics().densityDpi;
        if (dpi >= DisplayMetrics.DENSITY_XXHIGH) {
            return "axxh";
        } else if (dpi >= DisplayMetrics.DENSITY_XHIGH) {
            return "axh";
        } else if (dpi >= DisplayMetrics.DENSITY_HIGH) {
            return "ah";
        } else if (dpi >= DisplayMetrics.DENSITY_MEDIUM) {
            return "am";
        } else {
            return "am";
        }
    }

    public static void setStatusBarTranslucent(Activity activity) {
        if (Build.VERSION.SDK_INT == 19) {
            try {
                activity.getWindow().addFlags(0x04000000);
            } catch (Throwable e) {
            }
        }
    }

    // 获取手机状态栏高度
    public static int getStatusBarHeight(Context context) {
        Class<?> c;
        Object obj;
        Field field;
        int x;
        int statusBarHeight = 0;
        try {
            c = Class.forName("com.android.internal.R$dimen");
            obj = c.newInstance();
            field = c.getField("status_bar_height");
            x = Integer.parseInt(field.get(obj).toString());
            statusBarHeight = context.getResources().getDimensionPixelSize(x);
        } catch (Exception e) {

        }
        return statusBarHeight;
    }

    public static int getItemTopDistance(ListView listView, int position) {
        View topView = listView.getChildAt(position);
        if (topView == null) {
            return 0;
        }
        int top = -topView.getTop() <= topView.getMeasuredHeight() ? topView.getTop() : -topView.getMeasuredHeight();
        return -top;
    }

    public static int dip2px(Context context, float dipValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dipValue * scale + 0.5f);
    }

    public static void scrollToTop(final ListView listView) {
        scrollToTop(listView, 0);
    }

    public static void scrollToTop(final ListView listView, final int position) {
        if (listView.getFirstVisiblePosition() > LIST_VIEW_COME_BACK_TUNNEL) {
            listView.setSelection(LIST_VIEW_COME_BACK_TUNNEL / 2);
        }
        listView.post(new Runnable() {
            @Override
            public void run() {
                listView.smoothScrollToPosition(0);
            }
        });
    }

    public static Bitmap getCircleBitmap(Bitmap bitmap) {
        if (bitmap == null || bitmap.isRecycled())
            return null;
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        Bitmap rectBitmap = null;
        try {
            if (width > height) {
                rectBitmap = Bitmap.createBitmap(bitmap, (width - height) / 2, 0, height, height);
            } else {
                rectBitmap = Bitmap.createBitmap(bitmap, 0, (height - width) / 2, width, width);
            }
        } catch (OutOfMemoryError error) {
            return null;
        } catch (Exception e) {
        }
        Bitmap output = null;
        try {
            output = Bitmap.createBitmap(rectBitmap.getWidth(), rectBitmap.getHeight(), Bitmap.Config.ARGB_8888);
        } catch (OutOfMemoryError error) {
            return null;
        } catch (Exception e) {
        }
        if (output == null)
            return null;
        Canvas canvas = new Canvas(output);
        final int color = 0xff424242;
        final Paint paint = new Paint();
        final Rect rect = new Rect(0, 0, rectBitmap.getWidth(), rectBitmap.getHeight());
        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        paint.setColor(color);
        canvas.drawCircle(rectBitmap.getWidth() / 2, rectBitmap.getHeight() / 2, rectBitmap.getWidth() / 2, paint);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(rectBitmap, rect, rect, paint);
        if (rectBitmap != null && !rectBitmap.isRecycled()) {
            rectBitmap.recycle();
            rectBitmap = null;
        }
        canvas.setBitmap(null);
        System.gc();
        return output;
    }

    /**
     * @param bitmap
     * @param corner 1：左边，2:上边，3：右边，4：下边
     * @param radius
     * @return
     */

    public static Bitmap getTopCornerBitmap(Bitmap bitmap, int corner, float radius) {
        if (bitmap == null || bitmap.isRecycled())
            return null;
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();

        Bitmap output = null;
        try {
            output = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.ARGB_8888);
        } catch (OutOfMemoryError error) {
        } catch (Exception e) {
        }
        if (output == null)
            return null;
        Canvas canvas = new Canvas(output);
        final int color = 0xff424242;
        final Paint paint = new Paint();
        final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
        final RectF rectF = new RectF(rect);
        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        paint.setColor(color);

        Path path = new Path();
        float[] floats = new float[]{};
        path.addRoundRect(rectF, new float[]{radius, radius, radius, radius, 0f, 0f, 0f, 0f}, Path.Direction.CCW);
        canvas.drawPath(path, paint);

        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(bitmap, rect, rect, paint);
        if (bitmap != null && !bitmap.isRecycled()) {
            bitmap.recycle();
            bitmap = null;
        }
        canvas.setBitmap(null);
        System.gc();
        return output;
    }

    public static void setViewBackgroud(View view, int color) {
        int paddingLeft = view.getPaddingLeft();
        int paddingRight = view.getPaddingRight();
        int paddingTop = view.getPaddingTop();
        int paddingBottom = view.getPaddingBottom();
        view.setBackgroundColor(color);
        view.setPadding(paddingLeft, paddingTop, paddingRight, paddingBottom);
    }

    public static Bitmap drawableToBitmap(Drawable drawable) {
        int w = drawable.getIntrinsicWidth();
        int h = drawable.getIntrinsicHeight();
        Bitmap.Config config =
                drawable.getOpacity() != PixelFormat.OPAQUE ? Bitmap.Config.ARGB_8888
                        : Bitmap.Config.RGB_565;
        Bitmap bitmap = Bitmap.createBitmap(w, h, config);
        //注意，下面三行代码要用到，否在在View或者surfaceview里的canvas.drawBitmap会看不到图
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, w, h);
        drawable.draw(canvas);
        canvas.setBitmap(null);
        System.gc();
        return bitmap;
    }

    public static Bitmap viewToBitmap(View v) {
        v.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        v.measure(View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED), View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
        v.layout(0, 0, v.getMeasuredWidth(), v.getMeasuredHeight());
        Bitmap b = Bitmap.createBitmap(v.getMeasuredWidth(), v.getMeasuredHeight(), Bitmap.Config.ARGB_8888);

        Canvas c = new Canvas(b);
        v.draw(c);
        c.setBitmap(null);
        System.gc();
        return b;
    }

    public static Bitmap viewToBitmap2(View v) {
        Bitmap b = Bitmap.createBitmap(v.getMeasuredWidth(), v.getMeasuredHeight(), Bitmap.Config.ARGB_8888);

        Canvas c = new Canvas(b);
        v.draw(c);
        c.setBitmap(null);
        System.gc();
        return b;
    }

    public static Bitmap getSquareBitmap(Bitmap srcBmp) {
        if (null == srcBmp || srcBmp.isRecycled()) {
            return null;
        }

        Bitmap dstBmp;
        if (srcBmp.getWidth() >= srcBmp.getHeight()) {
            dstBmp = Bitmap.createBitmap(
                    srcBmp,
                    srcBmp.getWidth() / 2 - srcBmp.getHeight() / 2,
                    0,
                    srcBmp.getHeight(),
                    srcBmp.getHeight()
            );
        } else {
            dstBmp = Bitmap.createBitmap(
                    srcBmp,
                    0,
                    srcBmp.getHeight() / 2 - srcBmp.getWidth() / 2,
                    srcBmp.getWidth(),
                    srcBmp.getWidth()
            );
        }
        return dstBmp;
    }

    public static int getNavigationBarHeight(Context context) {
        Resources resources = context.getResources();
        int resourceId = resources.getIdentifier("navigation_bar_height", "dimen", "android");
        int height = resources.getDimensionPixelSize(resourceId);
        return height;
    }

    public static boolean checkDeviceHasNavigationBar(Context context) {

        boolean hasMenuKey = ViewConfiguration.get(context)
                .hasPermanentMenuKey();
        boolean hasBackKey = KeyCharacterMap
                .deviceHasKey(KeyEvent.KEYCODE_BACK);

        if (!hasMenuKey && !hasBackKey) {
            return true;
        }
        return false;
    }

    public static boolean inRangeOfView(View view, MotionEvent ev) {
        int[] location = new int[2];
        view.getLocationInWindow(location);
        int[] mLocation = new int[2];
        if (view.getParent() != null) {
            ((View) view.getParent()).getLocationInWindow(mLocation);
        }

        int width = view.getWidth();
        int height = view.getHeight();
        int x = location[0] - mLocation[0];
        int y = location[1] - mLocation[1];
        int ex = (int) ev.getX();
        int ey = (int) ev.getY();
        int bx = x + width;
        int by = y + height;
        if (ex < x || ex > bx || ey < y || ey > by) {
            return false;
        }
        return true;
    }

    public static boolean inRangeOfView(View view, int ex, int ey, int topY, int bottomY) {
        int[] location = new int[2];
        view.getLocationInWindow(location);
        int[] mLocation = new int[2];
        if (view.getParent() != null) {
            ((View) view.getParent()).getLocationInWindow(mLocation);
        }

        int width = view.getWidth();
        int height = view.getHeight();
        int x = location[0] - mLocation[0];
        int y = location[1] - mLocation[1];
        int bx = x + width;
        int by = y + height;
        if (ex < x || ex > bx || ey < y + topY || ey > by - bottomY) {
            return false;
        }
        return true;
    }

    /**
     * 读取图片属性：旋转的角度
     *
     * @param path 图片绝对路径
     * @return degree旋转的角度
     */
    public static int readPictureDegree(String path) {
        int degree = 0;
        try {
            ExifInterface exifInterface = new ExifInterface(path);
            int orientation = exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
            switch (orientation) {
                case ExifInterface.ORIENTATION_ROTATE_90:
                    degree = 90;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    degree = 180;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_270:
                    degree = 270;
                    break;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return degree;
    }

    /*
     * 旋转图片
     * @param angle
     * @param bitmap
     * @return Bitmap
     */
    public static Bitmap rotatingImageView(int angle, Bitmap bitmap) {
        //旋转图片 动作
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        // 创建新的图片
        Bitmap resizedBitmap = Bitmap.createBitmap(bitmap, 0, 0,
                bitmap.getWidth(), bitmap.getHeight(), matrix, true);
        return resizedBitmap;
    }

    public static Bitmap drawableToBitamp(Drawable drawable) {
        int w = drawable.getIntrinsicWidth();
        int h = drawable.getIntrinsicHeight();
        Bitmap.Config config =
                drawable.getOpacity() != PixelFormat.OPAQUE ? Bitmap.Config.ARGB_8888
                        : Bitmap.Config.RGB_565;
        Bitmap bitmap = Bitmap.createBitmap(w, h, config);
        //注意，下面三行代码要用到，否在在View或者surfaceview里的canvas.drawBitmap会看不到图
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, w, h);
        drawable.draw(canvas);
        return bitmap;
    }

}
