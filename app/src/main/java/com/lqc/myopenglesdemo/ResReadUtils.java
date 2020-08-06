package com.lqc.myopenglesdemo;

import android.content.res.Resources;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * @anchor: andy
 * @date: 2018-09-13
 * @description:
 */
public class ResReadUtils {

    /**
     * 读取资源
     *
     * @param resourceId
     * @return
     */
    public static String readResource(int resourceId) {
        float[] floats = new float[16];
        StringBuilder builder = new StringBuilder();
        try {
            InputStream inputStream = MyApp.sContext.getResources().openRawResource(resourceId);
            InputStreamReader streamReader = new InputStreamReader(inputStream);

            BufferedReader bufferedReader = new BufferedReader(streamReader);
            String textLine;
            while ((textLine = bufferedReader.readLine()) != null) {
                builder.append(textLine);
                builder.append("\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Resources.NotFoundException e) {
            e.printStackTrace();
        }
        return builder.toString();
    }

}