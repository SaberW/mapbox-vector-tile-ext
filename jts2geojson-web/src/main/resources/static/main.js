require.config({
    // baseUrl: '/content/js/lib',
    map: {
        '*': {
            'css': 'require/css'
        }
    },
    paths: {
        jQuery: './lib/jquery-1.11.3.min',
        bootstrap: './lib/bootstrap/js/bootstrap.min',
        ol: './lib/openlayers/ol-debug',
    },
    shim: {
        jQuery: {
            exports: 'jQuery'
        },
        bootstrap: {
            deps: [
                'jQuery',
                'css!./lib/bootstrap/css/bootstrap.min.css'
            ]
        },
        ol: {
            deps: ['css!./lib/openlayers/ol.css']
        }
    }
});