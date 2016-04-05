/**
 * Copyright (C) 2015-2016 Karoly Szanto
 *
 * This file is part of Peggy.
 *
 * Peggy is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Peggy is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Foobar.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.ksza.peggy.util;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.v4.graphics.drawable.DrawableCompat;

/**
 * Created by karoly.szanto on 12/07/15.
 */
public class UiUtil {

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public static Drawable setIconTint(final int drawableId, final int colorId, final Context context) {
        Drawable drawable = null;
        if(Util.isPreLolipop()) {
            drawable = context.getResources().getDrawable(drawableId);
        } else {
            drawable = context.getResources().getDrawable(drawableId, null);
        }

        Drawable wrap = DrawableCompat.wrap(drawable);
        DrawableCompat.setTint(wrap, context.getResources().getColor(colorId));

        return wrap;
    }
}
