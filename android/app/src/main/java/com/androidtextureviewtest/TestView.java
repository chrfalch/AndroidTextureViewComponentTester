package com.androidtextureviewtest;

import static android.opengl.EGL14.EGL_DEFAULT_DISPLAY;
import static javax.microedition.khronos.egl.EGL10.EGL_NO_CONTEXT;
import static javax.microedition.khronos.egl.EGL10.EGL_NO_DISPLAY;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.SurfaceTexture;
import android.opengl.GLES20;
import android.opengl.GLUtils;
import android.util.Log;
import android.view.TextureView;
import android.widget.LinearLayout;

import com.facebook.react.views.view.ReactViewGroup;

import javax.microedition.khronos.egl.EGL10;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.egl.EGLContext;
import javax.microedition.khronos.egl.EGLDisplay;
import javax.microedition.khronos.egl.EGLSurface;

public class TestView extends ReactViewGroup implements TextureView.SurfaceTextureListener {
    class RenderThread {
        private static final int EGL_CONTEXT_CLIENT_VERSION = 0x3098;
        private static final int EGL_OPENGL_ES2_BIT = 4;

        private EGLDisplay display = EGL_NO_DISPLAY;
        private EGLConfig config = null;
        private EGLContext context = EGL_NO_CONTEXT;
        private EGLSurface surface = null;

        boolean isStopped = false;

        SurfaceTexture mSurface;

        RenderThread(SurfaceTexture s) {
            mSurface = s;

            EGL10 egl = (EGL10) EGLContext.getEGL();
            display = egl.eglGetDisplay(EGL_DEFAULT_DISPLAY);

            int[] version = new int[2];
            egl.eglInitialize(display, version);   // getting OpenGL ES 2
            config = chooseEglConfig(egl, display);

            int[] attrib_list = { EGL_CONTEXT_CLIENT_VERSION, 2, EGL10.EGL_NONE };
            context = egl.eglCreateContext(display, config,
                    EGL_NO_CONTEXT, attrib_list);

            surface = egl.eglCreateWindowSurface(display, config, mSurface, null);
            egl.eglMakeCurrent(display, surface, surface, context);
        }

        private int[] getConfig() {
            return new int[] {
                    EGL10.EGL_RENDERABLE_TYPE, EGL_OPENGL_ES2_BIT,
                    EGL10.EGL_RED_SIZE, 8,
                    EGL10.EGL_GREEN_SIZE, 8,
                    EGL10.EGL_BLUE_SIZE, 8,
                    EGL10.EGL_ALPHA_SIZE, 8,
                    EGL10.EGL_DEPTH_SIZE, 0,
                    EGL10.EGL_STENCIL_SIZE, 0,
                    EGL10.EGL_NONE
            };
        }

        public void drawFrame(float color) {
            EGL10 egl = (EGL10) EGLContext.getEGL();
            GLES20.glClearColor(color / 2, color, color, 1.0f);
            GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
            egl.eglSwapBuffers(display, surface);
        }

        public void run() {
            EGL10 egl = (EGL10) EGLContext.getEGL();

           egl.eglMakeCurrent(display, surface, surface, context);
            float colorVelocity = 0.01f;
            float color = 0.5f;

            while (!isStopped) {
                if (color > 1 || color < 0) colorVelocity *= -1;
                color += colorVelocity;
                drawFrame(color);
                try {
                    Thread.sleep((int) (1f / 60f * 1000f)); // in real life this sleep is more complicated
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }

            mSurface.release();
            egl.eglDestroyContext(display, context);
            egl.eglDestroySurface(display, surface);
        }

        private EGLConfig chooseEglConfig(EGL10 egl, EGLDisplay eglDisplay) {
            int[] configsCount = new int[1];
            EGLConfig[] configs = new EGLConfig[1];
            int[] configSpec = getConfig();

            if (!egl.eglChooseConfig(eglDisplay, configSpec, configs, 1, configsCount)) {
                throw new IllegalArgumentException("eglChooseConfig failed " +
                        GLUtils.getEGLErrorString(egl.eglGetError()));
            } else if (configsCount[0] > 0) {
                return configs[0];
            }
            return null;
        }
    }

    private TextureView mTextureView;

    private RenderThread renderer;

    public TestView(Context context) {
        super(context);

        mTextureView = new TextureView(getContext().getApplicationContext());
        addView(mTextureView);

        mTextureView.setSurfaceTextureListener(this);
        mTextureView.setOpaque(false);
        setBackgroundColor(Color.GREEN);
    }

    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
        Log.i("SkiaBaseView", "onSurfaceTextAvailable - rendering OpenGL");
        mTextureView.layout(0, 0, getMeasuredWidth(), getMeasuredHeight());
        renderer = new RenderThread(surface);
        renderer.drawFrame(0.5f);
        Log.i("SkiaBaseView", "onSurfaceTextAvailable - done.");
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {

    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
        renderer.isStopped = true;
        return false;                // surface.release() manually, after the last render
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surface) {
        // Nothing special to do here
    }
}
