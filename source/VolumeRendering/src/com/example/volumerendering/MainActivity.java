package com.example.volumerendering;

import android.app.Activity;
import android.opengl.GLSurfaceView;
import android.os.Bundle;

public class MainActivity extends Activity {

	private GLSurfaceView surfaceView;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		surfaceView = (GLSurfaceView) findViewById(R.viewer.gl_surface_view);
		surfaceView.setRenderer(new GLRenderer(this));
	}

}