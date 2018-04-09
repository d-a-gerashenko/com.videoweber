;$(function() {
    var scrollToTopButton = $('<span id="chw-scroll-top" class="hidden"><a href="#top" class="well well-sm"><i class="glyphicon glyphicon-chevron-up"></i></a></span>');
    scrollToTopButton.click(function (e) {
        e.preventDefault();
        $('html,body').animate({scrollTop:0},'slow');
    });
    $(document.body).append(scrollToTopButton);
    // Only enable if the document has a long scroll bar
    // Note the window height + offset
    if ( ($(window).height() + 100) < $(document).height() ) {
        scrollToTopButton.removeClass('hidden').affix({
            // how far to scroll down before link "slides" into view
            offset: {top:100}
        });
    }
});