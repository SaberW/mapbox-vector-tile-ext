require.config({
    // baseUrl: '/content/js/lib',
    map: {
        '*': {
            'css': 'require/css'
        }
    },
    paths: {
        app: '../app',
        jquery: 'lib/jquery-1.11.3.min',
        bootstrap: 'bootstrap/bootstrap.min',
        ol: 'lib/openlayers/ol',
    },
    shim: {
        bootstrap: {
            deps: [
                'jquery',
                'css!../../bootstrap.min.css'
            ]
        },
    }
});