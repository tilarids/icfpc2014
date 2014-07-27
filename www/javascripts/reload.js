$(function() {
    reload()
})

function reload() {
    var ghosts = $('.g')
    check('../solution/lambdaman.gcc', '#lambda')
    check('../solution/ghost0.ghc', ghosts[0])
    check('../solution/ghost1.ghc', ghosts[1])
    check('../solution/ghost2.ghc', ghosts[2])
    check('../solution/ghost3.ghc', ghosts[3])
    check('../solution/maps/map0.txt', '#map')

    setTimeout(reload, 1000)
}

function check(url, el) {
    $.ajax(url).then(function(x) {
        var $el = $(el)
        if (x && x != $el.text()) {
            $el.text(x)
            loaded(url)
        }
    })
}

function loaded(url) {
    $loaded = $('#loaded')
    if (!$loaded.is(":visible")) {
        $loaded.html('Loaded')
    }
    $loaded.html($loaded.html() + '<br>' + url)
    $loaded.show()
    $loaded.fadeOut(10000)
    load()
}
