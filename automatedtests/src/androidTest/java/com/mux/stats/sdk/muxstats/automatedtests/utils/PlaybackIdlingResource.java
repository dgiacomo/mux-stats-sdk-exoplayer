package com.mux.stats.sdk.muxstats.automatedtests.utils;

import androidx.test.espresso.IdlingResource;

public class PlaybackIdlingResource implements IdlingResource, PlaybackIdlingListener {

  private boolean isIdle = true;
  private IdlingResource.ResourceCallback resourceCallback = null;


  @Override
  public String getName() {
    return PlaybackIdlingResource.class.getSimpleName();
  }

  @Override
  public boolean isIdleNow() {
    return isIdle;
  }

  @Override
  public void registerIdleTransitionCallback(ResourceCallback callback) {
    resourceCallback = callback;
  }

  @Override
  public void pauseStarted() {
    isIdle = false;
  }

  @Override
  public void pauseEnded() {
    isIdle = true;
    resourceCallback.onTransitionToIdle();
  }
}
