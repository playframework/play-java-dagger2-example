import java.time.Clock;
import java.util.HashMap;

import javax.inject.Inject;
import javax.inject.Singleton;

import controllers.HomeController;
import dagger.Component;
import dagger.Module;
import dagger.Provides;
import play.DefaultApplication;
import play.api.OptionalSourceMapper;
import play.api.http.DefaultHttpFilters;
import play.api.http.HttpRequestHandler;
import play.api.http.JavaCompatibleHttpRequestHandler;
import play.api.inject.SimpleInjector;
import play.core.j.DefaultJavaHandlerComponents;
import play.http.DefaultActionCreator;
import play.http.DefaultHttpErrorHandler;
import play.inject.Injector;
import play.libs.Scala;

class MyComponentsFromContext extends play.api.BuiltInComponentsFromContext {

    private final HomeController homeController;

    @Inject
    public MyComponentsFromContext(play.ApplicationLoader.Context context, HomeController homeController) {
        super(context.underlying());
        this.homeController = homeController;
    }

    @Override
    public HttpRequestHandler httpRequestHandler() {
        return new JavaCompatibleHttpRequestHandler(
            router(),
            httpErrorHandler(),
            httpConfiguration(),
            new DefaultHttpFilters(httpFilters()),
            new DefaultJavaHandlerComponents(injector(), new DefaultActionCreator())
        );
    }

    @Override
    public play.api.routing.Router router() {
        return new router.Routes(httpErrorHandler(), homeController);
    }

    @Override
    public play.api.inject.Injector injector() {
        // We need to add any Java actions and body parsers needed to the runtime injector
        return new SimpleInjector(super.injector(), Scala.asScala(new HashMap<Class<?>, Object>() {{
            put(play.mvc.BodyParser.Default.class, new play.mvc.BodyParser.Default(javaErrorHandler(), httpConfiguration()));
        }}));
    }

    public play.http.HttpErrorHandler javaErrorHandler() {
        return new DefaultHttpErrorHandler(
            new play.Configuration(configuration().underlying()),
            new play.Environment(environment()),
            new OptionalSourceMapper(sourceMapper()),
            () -> router()
        );
    }

    public play.Application javaApplication() {
        Injector injector = new play.inject.DelegateInjector(super.injector());
        return new DefaultApplication(super.application(), injector);
    }
}


@Singleton
@Component(modules = { ApplicationModule.class })
interface MyComponentsFactory {
   MyComponentsFromContext componentsFromContext();
}

@Module
class ApplicationModule {

    private final play.ApplicationLoader.Context context;

    public ApplicationModule(play.ApplicationLoader.Context context) {
        this.context = context;
    }

    @Provides
    play.ApplicationLoader.Context providesContext() {
        return this.context;
    }

    @Provides
    public Clock providesClock() {
        return java.time.Clock.systemUTC();
    }
}
