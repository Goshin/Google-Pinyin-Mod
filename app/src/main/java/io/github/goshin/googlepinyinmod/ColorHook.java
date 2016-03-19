package io.github.goshin.googlepinyinmod;

import android.content.res.XResources;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RectShape;
import android.graphics.drawable.shapes.RoundRectShape;

import java.util.Map;

import de.robv.android.xposed.IXposedHookInitPackageResources;
import de.robv.android.xposed.XSharedPreferences;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.callbacks.XC_InitPackageResources;

public class ColorHook implements IXposedHookInitPackageResources {

    private static final String GOOGLE_PINYIN_PACKAGE = "com.google.android.inputmethod.pinyin";
    private float scale;
    private boolean verbose = false;

    private float dpToPx(int dp) {
        return dp * scale + 0.5f;
    }

    private void log(String text) {
        if (verbose) {
            XposedBridge.log(text);
        }
    }

    private void log(Throwable throwable) {
        if (verbose) {
            XposedBridge.log(throwable);
        }
    }

    @Override
    public void handleInitPackageResources(XC_InitPackageResources.InitPackageResourcesParam resParam) throws Throwable {
        if (!resParam.packageName.toLowerCase().equals(GOOGLE_PINYIN_PACKAGE)) {
            return;
        }

        log("hook " + resParam.packageName);

        XSharedPreferences pref = new XSharedPreferences(this.getClass().getPackage().getName(), "pref");
        pref.makeWorldReadable();
        pref.reload();
        Map<String, ?> keys = pref.getAll();

        for (Map.Entry<String, ?> entry : keys.entrySet()) {
            try {
                String colorName = entry.getKey();
                int color = (Integer) entry.getValue();
                resParam.res.setReplacement(resParam.packageName, "color", colorName, color);
            } catch (Exception e) {
                log(e);
            }
        }

        scale = pref.getFloat("scale", 0);

        for (String style : new String[]{"light", "dark"}) {
            final int composingColorReplacement = pref.getInt("composing_material_" + style + "_theme", -1);
            if (composingColorReplacement != -1) {
                resParam.res.setReplacement(resParam.packageName, "drawable", "bg_composing_text_material_" + style + "_theme", new XResources.DrawableLoader() {
                    @Override
                    public Drawable newDrawable(XResources xResources, int i) throws Throwable {
                        float[] radii = {0, 0, dpToPx(6), dpToPx(6), 0, 0, 0, 0};
                        log("dp to px 6:" + dpToPx(6));
                        ShapeDrawable shapeDrawable = new ShapeDrawable(new RoundRectShape(radii, null, null));
                        shapeDrawable.getPaint().setColor(composingColorReplacement);
                        return shapeDrawable;
                    }
                });
            }

            final int candidateColorReplacement = pref.getInt("header_candidate_material_" + style + "_theme", -1);
            if (candidateColorReplacement == -1) {
                continue;
            }
            XResources.DrawableLoader threeItemLayer = new XResources.DrawableLoader() {
                @Override
                public Drawable newDrawable(XResources res, int id) throws Throwable {
                    ShapeDrawable top = new ShapeDrawable(new RectShape());
                    top.getPaint().setColor(candidateColorReplacement);
                    ShapeDrawable mid = new ShapeDrawable((new RectShape()));
                    mid.getPaint().setColor(0x0dffffff);
                    ShapeDrawable bottom = new ShapeDrawable(new RectShape());
                    bottom.getPaint().setColor(candidateColorReplacement);
                    return new LayerDrawable(new Drawable[]{top, mid, bottom});
                }
            };
            resParam.res.setReplacement(resParam.packageName, "drawable", "bg_header_material_" + style + "_theme", threeItemLayer);
            resParam.res.setReplacement(resParam.packageName, "drawable", "bg_more_candidates_left_panel_material_" + style + "_theme", threeItemLayer);
            resParam.res.setReplacement(resParam.packageName, "drawable", "bg_more_candidates_right_panel_material_" + style + "_theme", threeItemLayer);

            resParam.res.setReplacement(resParam.packageName, "drawable", "bg_more_candidates_holder_material_" + style + "_theme", new XResources.DrawableLoader() {
                @Override
                public Drawable newDrawable(XResources res, int id) throws Throwable {
                    ShapeDrawable shapeDrawable = new ShapeDrawable(new RectShape());
                    shapeDrawable.getPaint().setColor(candidateColorReplacement);
                    return shapeDrawable;
                }
            });

            resParam.res.setReplacement(resParam.packageName, "drawable", "bg_non_prime_keyboard_page_indicator_material_" + style + "_theme", new XResources.DrawableLoader() {
                @Override
                public Drawable newDrawable(XResources res, int id) throws Throwable {
                    ShapeDrawable shapeDrawable = new ShapeDrawable(new RectShape());
                    shapeDrawable.getPaint().setColor(candidateColorReplacement & 0xccffffff);
                    return shapeDrawable;
                }
            });
        }

        log("hook color completed");
    }
}
