package com.mux.stats.sdk.muxstats.automatedtests;

import static org.junit.Assert.fail;

import com.mux.stats.sdk.core.events.playback.RebufferEndEvent;
import com.mux.stats.sdk.core.events.playback.RebufferStartEvent;
import com.mux.stats.sdk.core.model.CustomerVideoData;
import com.mux.stats.sdk.muxstats.MuxStatsExoPlayer;
import org.junit.Before;
import org.junit.Test;

public class VideoChangeTests extends TestBase {

  int numberOfVideoChanges = 0;

//  @Before
//  public void init() {
//    urlToPlay = "http://localhost:5000/hls/google_glass/playlist.m3u8";
//    super.init();
//  }

  @Test
  public void testErrorPropagationOnVideoChange() {
    try {
      if (!testActivity.waitForPlaybackToStart(waitForPlaybackToStartInMS)) {
        fail("Playback did not start in " + waitForPlaybackToStartInMS + " milliseconds !!!");
      }
      Thread.sleep(PLAY_PERIOD_IN_MS);
      httpServer.returnForbiddenOnEveryRequest(true);
      changeVideo(true, "http://localhost:5000/vod.mp4");
      Thread.sleep(PAUSE_PERIOD_IN_MS * 100);
      if(!testActivity.waitForPlaybackError(PLAY_PERIOD_IN_MS)) {
        fail("Playback error did not opccure in " + PLAY_PERIOD_IN_MS + " milliseconds !!!");
      }
      httpServer.returnForbiddenOnEveryRequest(false);
      changeVideo(true, "http://localhost:5000/vod.mp4");
      Thread.sleep(PLAY_PERIOD_IN_MS);
      // TODO check if there is video erro message and code
    } catch (Exception e) {
      fail(getExceptionFullTraceAndMessage(e));
    }
  }

  @Test
  public void testVideoChange() {
    testVideoChange(true);
  }

  @Test
  public void testProgramChange() {
    testVideoChange(false);
  }

  /**
   *
   * @param videoChange, if set to true we will use video change function, if false we will use
   *                     programe change function
   */
  public void testVideoChange(boolean videoChange) {
    try {
      if (!testActivity.waitForPlaybackToStart(waitForPlaybackToStartInMS)) {
        fail("Playback did not start in " + waitForPlaybackToStartInMS + " milliseconds !!!");
      }
      Thread.sleep(PAUSE_PERIOD_IN_MS);
      // Video started, do video change, we expect to see fake rebufferstart
      changeVideo(videoChange, "http://localhost:5000/hls/google_glass/playlist.m3u8");
      Thread.sleep(PAUSE_PERIOD_IN_MS);
      int rebufferStartEventIndex = 0;
      int rebufferEndEventIndex;
      while ((rebufferStartEventIndex = networkRequest.getIndexForNextEvent(
          rebufferStartEventIndex, RebufferStartEvent.TYPE)) != -1) {
        rebufferEndEventIndex = networkRequest.getIndexForNextEvent(rebufferStartEventIndex,
            RebufferEndEvent.TYPE);
        if (rebufferEndEventIndex == -1) {
          fail("We have rebuffer start event at position: " + rebufferStartEventIndex
              + ",without matching rebuffer end event, events: "
              + networkRequest.getReceivedEventNames());
        }
      }
    } catch (Exception e) {
      fail(getExceptionFullTraceAndMessage(e));
    }
  }

//  private void playNextVideo(String url) {
//    testActivity.runOnUiThread(new Runnable() {
//      public void run() {
//        testActivity.setUrlToPlay(url);
//        testActivity.startPlayback();
//      }
//    });
//  }

  private void changeVideo(final boolean videoChange, String url) {
    testActivity.runOnUiThread(new Runnable() {
      public void run() {
        testActivity.setUrlToPlay(url);
        CustomerVideoData customerVideoData = new CustomerVideoData();
        customerVideoData.setVideoTitle(BuildConfig.FLAVOR + "-" + currentTestName.getMethodName()
            + "_title_" + numberOfVideoChanges);
        numberOfVideoChanges++;
        MuxStatsExoPlayer muxStats = testActivity.getMuxStats();
        if (videoChange) {
          muxStats.videoChange(customerVideoData);
        } else {
          muxStats.programChange(customerVideoData);
        }
        testActivity.startPlayback();
      }
    });
  }
}
