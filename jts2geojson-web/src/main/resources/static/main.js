require.config({
    baseUrl: contextPath,
    map: {
        '*': {
            'css': 'lib/css.min.js',
            'text': 'lib/text.min.js',
            'json': 'lib/json.min.js',
        }
    },
    // urlArgs:'v='+(new Date()).getTime(),//清除缓存
    paths: {
        'jQuery': 'lib/jquery-1.11.3.min',
        'bootstrap': 'lib/bootstrap/js/bootstrap.min',
        'ol': 'lib/openlayers/ol',
        'map': 'model/map'
    },
    shim: {
        'jQuery': {
            exports: 'jQuery'
        },
        'bootstrap': {
            deps: [
                'jQuery',
                'css!lib/bootstrap/css/bootstrap.min.css'
            ]
        },
        'ol': {
            deps: ['css!lib/openlayers/ol.css']
        },
        'map': {
            deps: ['jQuery', 'bootstrap', 'ol']
        }
    }
});

require(['map'])