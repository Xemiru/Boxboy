/*
 * MIT License
 *
 * Copyright (c) 2018 Tellerva, Marc Lawrence
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package com.github.xemiru.sponge.boxboy.util;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;

/**
 * Reflection utility class.
 */
public class ReflectUtil {

    private ReflectUtil() {
    }

    public static Method getDeclaredMethod(String sourceClass, String[] names, String... parameterClasses)
        throws ClassNotFoundException, NoSuchMethodException {
        Class<?> src = Class.forName(sourceClass);
        Class[] params = new Class[parameterClasses.length];
        for (int i = 0; i < parameterClasses.length; i++)
            params[i] = Class.forName(parameterClasses[i]);

        for (String name : names) {
            try {
                Method method = src.getDeclaredMethod(name, params);
                method.setAccessible(true);
                return method;
            } catch (NoSuchMethodException ignored) {
            }
        }

        throw new NoSuchMethodException(Arrays.toString(names));
    }

    public static Field getDeclaredField(String sourceClass, String... names)
        throws ClassNotFoundException, NoSuchFieldException {
        Class<?> src = Class.forName(sourceClass);

        for (String name : names) {
            try {
                Field field = src.getDeclaredField(name);
                field.setAccessible(true);
                return field;
            } catch (NoSuchFieldException ignored) {
            }
        }

        throw new NoSuchFieldException(Arrays.toString(names));
    }
}
