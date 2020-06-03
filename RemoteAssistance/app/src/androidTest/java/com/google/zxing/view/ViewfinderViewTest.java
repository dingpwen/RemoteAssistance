package com.google.zxing.view;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static androidx.test.core.app.ApplicationProvider.getApplicationContext;
import static org.junit.Assert.*;

@RunWith(AndroidJUnit4.class)
public class ViewfinderViewTest {
    private ViewfinderView finderView;

    @Before
    public void setUp() throws Exception {
        finderView = new ViewfinderView(getApplicationContext(), null);
    }

    @Test
    public void shadeColor() {
        int color = 0xFF3456;
        color = finderView.shadeColor(color);
        assertEquals(0x20FF3456, color);

        color = 0x903456;
        color = finderView.shadeColor(color);
        assertEquals(0x20903456, color);

        color = 0xF0903456;
        color = finderView.shadeColor(color);
        assertEquals(0x20903456, color);
    }
}