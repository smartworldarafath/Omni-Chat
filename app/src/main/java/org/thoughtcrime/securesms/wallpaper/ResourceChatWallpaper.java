package org.thoughtcrime.securesms.wallpaper;

import android.content.Context;
import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;
import android.widget.ImageView;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;

import org.thoughtcrime.securesms.database.model.databaseprotos.Wallpaper;
import org.thoughtcrime.securesms.dependencies.AppDependencies;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public final class ResourceChatWallpaper implements ChatWallpaper, Parcelable {

  public static final ResourceChatWallpaper CUSTOM_1  = new ResourceChatWallpaper("custom_wallpaper_1", org.thoughtcrime.securesms.R.drawable.custom_wallpaper_1, 0f);
  public static final ResourceChatWallpaper CUSTOM_2  = new ResourceChatWallpaper("custom_wallpaper_2", org.thoughtcrime.securesms.R.drawable.custom_wallpaper_2, 0f);
  public static final ResourceChatWallpaper CUSTOM_3  = new ResourceChatWallpaper("custom_wallpaper_3", org.thoughtcrime.securesms.R.drawable.custom_wallpaper_3, 0f);
  public static final ResourceChatWallpaper CUSTOM_4  = new ResourceChatWallpaper("custom_wallpaper_4", org.thoughtcrime.securesms.R.drawable.custom_wallpaper_4, 0f);
  public static final ResourceChatWallpaper CUSTOM_5  = new ResourceChatWallpaper("custom_wallpaper_5", org.thoughtcrime.securesms.R.drawable.custom_wallpaper_5, 0f);
  public static final ResourceChatWallpaper CUSTOM_6  = new ResourceChatWallpaper("custom_wallpaper_6", org.thoughtcrime.securesms.R.drawable.custom_wallpaper_6, 0f);
  public static final ResourceChatWallpaper CUSTOM_7  = new ResourceChatWallpaper("custom_wallpaper_7", org.thoughtcrime.securesms.R.drawable.custom_wallpaper_7, 0f);
  public static final ResourceChatWallpaper CUSTOM_8  = new ResourceChatWallpaper("custom_wallpaper_8", org.thoughtcrime.securesms.R.drawable.custom_wallpaper_8, 0f);
  public static final ResourceChatWallpaper CUSTOM_9  = new ResourceChatWallpaper("custom_wallpaper_9", org.thoughtcrime.securesms.R.drawable.custom_wallpaper_9, 0f);
  public static final ResourceChatWallpaper CUSTOM_10 = new ResourceChatWallpaper("custom_wallpaper_10", org.thoughtcrime.securesms.R.drawable.custom_wallpaper_10, 0f);

  private final String resourceName;
  private final int    resourceId;
  private final float  dimLevelInDarkTheme;

  private ResourceChatWallpaper(@NonNull String resourceName, @DrawableRes int resourceId, float dimLevelInDarkTheme) {
    this.resourceName        = resourceName;
    this.resourceId          = resourceId;
    this.dimLevelInDarkTheme = dimLevelInDarkTheme;
  }

  public static @NonNull List<ChatWallpaper> getCustomBuiltIns() {
    return Arrays.asList(CUSTOM_1, CUSTOM_2, CUSTOM_3, CUSTOM_4, CUSTOM_5, CUSTOM_6, CUSTOM_7, CUSTOM_8, CUSTOM_9, CUSTOM_10);
  }

  static boolean isResourceUri(@NonNull Uri uri) {
    return "android.resource".equals(uri.getScheme());
  }

  static @NonNull ResourceChatWallpaper fromUri(@NonNull Uri uri, float dimLevelInDarkTheme) {
    String resourceName = uri.getLastPathSegment();
    if (resourceName == null || resourceName.isEmpty()) {
      throw new IllegalArgumentException("Missing wallpaper resource name: " + uri);
    }

    Context context = AppDependencies.getApplication();
    int resourceId = context.getResources().getIdentifier(resourceName, "drawable", context.getPackageName());
    if (resourceId == 0) {
      throw new IllegalArgumentException("Unknown wallpaper resource: " + uri);
    }

    return new ResourceChatWallpaper(resourceName, resourceId, dimLevelInDarkTheme);
  }

  @Override
  public float getDimLevelForDarkTheme() {
    return dimLevelInDarkTheme;
  }

  @Override
  public void loadInto(@NonNull ImageView imageView) {
    imageView.setImageResource(resourceId);
  }

  @Override
  public boolean isPhoto() {
    return true;
  }

  @Override
  public boolean isSameSource(@NonNull ChatWallpaper chatWallpaper) {
    return chatWallpaper instanceof ResourceChatWallpaper &&
           resourceName.equals(((ResourceChatWallpaper) chatWallpaper).resourceName);
  }

  @Override
  public @NonNull Wallpaper serialize() {
    Context context = AppDependencies.getApplication();
    Uri uri = new Uri.Builder()
        .scheme("android.resource")
        .authority(context.getPackageName())
        .appendPath("drawable")
        .appendPath(resourceName)
        .build();

    return new Wallpaper.Builder()
        .file_(new Wallpaper.File.Builder().uri(uri.toString()).build())
        .dimLevelInDarkTheme(dimLevelInDarkTheme)
        .build();
  }

  @Override
  public int describeContents() {
    return 0;
  }

  @Override
  public void writeToParcel(Parcel dest, int flags) {
    dest.writeString(resourceName);
    dest.writeInt(resourceId);
    dest.writeFloat(dimLevelInDarkTheme);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    ResourceChatWallpaper that = (ResourceChatWallpaper) o;
    return resourceId == that.resourceId &&
           Float.compare(that.dimLevelInDarkTheme, dimLevelInDarkTheme) == 0 &&
           resourceName.equals(that.resourceName);
  }

  @Override
  public int hashCode() {
    return Objects.hash(resourceName, resourceId, dimLevelInDarkTheme);
  }

  public static final Creator<ResourceChatWallpaper> CREATOR = new Creator<ResourceChatWallpaper>() {
    @Override
    public ResourceChatWallpaper createFromParcel(Parcel in) {
      return new ResourceChatWallpaper(in.readString(), in.readInt(), in.readFloat());
    }

    @Override
    public ResourceChatWallpaper[] newArray(int size) {
      return new ResourceChatWallpaper[size];
    }
  };
}
