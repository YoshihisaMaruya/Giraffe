export PKG_CONFIG_PATH=/opt/local/lib/pkgconfig
export LD_LIBRARY_PATH=/opt/local/lib
g++ -bind_at_load -fPIC -shared `pkg-config --cflags opencv` Surf.cpp -o libsurf.so `pkg-config --libs opencv`
g++ -bind_at_load -fPIC -shared `pkg-config --cflags opencv` LshMatcher.cpp -o liblsh_matcher.so `pkg-config --libs opencv`
