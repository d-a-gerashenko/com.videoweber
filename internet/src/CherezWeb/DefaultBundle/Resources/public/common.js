$(function() {
    // Анимация прокрутки при переходе по якарям.
    var scrollToAnchor = function() {
        try {
            var windowHash = window.location.hash;
            if (windowHash.length > 1) { // # + текст = минимум 2 символа
                var hash = '#scroll-to-' + windowHash.slice(1);
                var target = $(hash);
                target = target.length ? target : $('[name=' + hash.slice(1) +']');
                if (target.length) {
                    $('html,body').animate({
                        scrollTop: target.offset().top - $('nav').outerHeight()
                    }, 'slow');
                }
            }
        } catch(err) {};
    };
    
    // При смене хеша в url скролим к якорю.
    $(window).on('hashchange', function(e){
        scrollToAnchor();
    });
    
    /**
     * Если при клике по ссылке смена хеша не происходит (уже стоит нужный хеш),
     * скорилм вручную.
     */
    $('a[href*=#]:not([href=#])').click(function(e) {
        if (window.location.pathname.replace(/^\//,'') === this.pathname.replace(/^\//,'') && window.location.hostname === this.hostname) {
            if (window.location.hash === this.hash) {
                scrollToAnchor();
                return false;
            }
        }
    });
    
    // После загрузки страницы скролим к якорю
    scrollToAnchor();
});
function getDefaultAjaxDialogParams () {
    return {
        modal: true,
//            dialogClass: 'fixed-dialog', // Чтобы форма не реагировала на прокрутку мышки.
        draggable: false,
        resizable: false,
//        buttons: [
//            {text: "Закрыть", click: function() {
//                $(this).dialog('close');
//            }}
//        ],
        close: function(event, ui){
            $(this).dialog('destroy').remove();
        },
        width: 600
    };
}