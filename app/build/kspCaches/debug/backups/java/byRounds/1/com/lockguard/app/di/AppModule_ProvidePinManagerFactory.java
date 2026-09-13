package com.lockguard.app.di;

import android.content.Context;
import com.lockguard.app.data.security.PinManager;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

@ScopeMetadata("javax.inject.Singleton")
@QualifierMetadata("dagger.hilt.android.qualifiers.ApplicationContext")
@DaggerGenerated
@Generated(
    value = "dagger.internal.codegen.ComponentProcessor",
    comments = "https://dagger.dev"
)
@SuppressWarnings({
    "unchecked",
    "rawtypes",
    "KotlinInternal",
    "KotlinInternalInJava",
    "cast"
})
public final class AppModule_ProvidePinManagerFactory implements Factory<PinManager> {
  private final Provider<Context> contextProvider;

  public AppModule_ProvidePinManagerFactory(Provider<Context> contextProvider) {
    this.contextProvider = contextProvider;
  }

  @Override
  public PinManager get() {
    return providePinManager(contextProvider.get());
  }

  public static AppModule_ProvidePinManagerFactory create(Provider<Context> contextProvider) {
    return new AppModule_ProvidePinManagerFactory(contextProvider);
  }

  public static PinManager providePinManager(Context context) {
    return Preconditions.checkNotNullFromProvides(AppModule.INSTANCE.providePinManager(context));
  }
}
