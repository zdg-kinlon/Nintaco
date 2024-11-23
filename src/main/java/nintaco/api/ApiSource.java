package nintaco.api;

import nintaco.App;
import nintaco.api.local.LocalAPI;
import nintaco.api.remote.RemoteAPI;

import static nintaco.util.StringUtil.isBlank;

/**
 * <p>A factory that provides the handle to the API and the way to setup the
 * Remote API. Only one instance of the API exists during the lifetime of a
 * program run; as far as a program is concerned, the API is a singleton and
 * this factory provides the means of getting a reference to it. Classes
 * interested in using the API can declare an object constant via the following
 * code to make the reference available to all methods.</p>
 *
 * <p>{@code private final API api = ApiSource.getAPI();}</p>
 *
 * <p>For the Remote API, {@link ApiSource#initRemoteAPI(java.lang.String, int)
 * initRemoteAPI} must be invoked prior to any of the calls to
 * {@link ApiSource#getAPI() getAPI}.</p>
 */
public final class ApiSource {

    private static RemoteAPI remoteAPI;
    private static String host;
    private static int port;

    private ApiSource() {
    }

    /**
     * Provides the handle to the API. The API is not enabled until {@link
     * API#run()} is called.
     *
     * @return The handle to the API. Or, {@code null} when used remotely
     * prior to calling {@link #initRemoteAPI(java.lang.String, int)
     * initRemoteAPI}.
     * @see #initRemoteAPI(java.lang.String, int)
     * @see API#run()
     */
    public static synchronized API getAPI() {

        if (remoteAPI == null && !isBlank(host) && !App.isEmulator()) {
            remoteAPI = new RemoteAPI(host, port);
        }

        return remoteAPI == null ? LocalAPI.getLocalAPI() : remoteAPI;
    }

    /**
     * Initializes the Remote API. This method should be invoked near the
     * program entrypoint, prior to any calls to {@link #getAPI() getAPI}.
     * Calling this method informs the factory that it should provide a
     * remote proxy implementation. However, a socket connection is not
     * established until {@link API#run()} is called. In between {@code
     * initRemoteAPI()} and {@code run()}, listeners should be added.
     *
     * @param host The hostname of the machine on which the Program Server is
     *             running.
     * @param port The port number on which the Program Server is listening.
     * @see #getAPI()
     * @see API#run()
     */
    public static synchronized void initRemoteAPI(final String host,
                                                  final int port) {
        ApiSource.host = host;
        ApiSource.port = port;
    }
}
