package com.github.jacks.planetaryIdle.gwt;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.backends.gwt.GwtApplication;
import com.badlogic.gdx.backends.gwt.GwtApplicationConfiguration;
import com.badlogic.gdx.utils.GdxRuntimeException;
// import com.github.jacks.planetaryIdle.PlanetaryIdle;

/** Launches the GWT application. */
public class GwtLauncher extends GwtApplication {
    @Override
    public GwtApplicationConfiguration getConfig () {
        return new GwtApplicationConfiguration(1080, 760);
    }

    @Override
    public ApplicationListener createApplicationListener () {
        throw new GdxRuntimeException("Kotlin is currently not supported by GWT.");
        // return new PlanetaryIdle();
    }
}
