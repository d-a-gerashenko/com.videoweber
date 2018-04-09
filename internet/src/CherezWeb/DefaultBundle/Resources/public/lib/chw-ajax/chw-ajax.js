;(function($, doc, win) {
    "use strict";

    var ChwAjax = (function() {
        var instance;

        // Приватные свойства
        //var privateVar = '';
        var ajaxResponseListClass = 'chw-ajax-response-list';
        var ajaxModalClass = 'chw-ajax-modal';
        var ajaxPendingClass = 'chw-ajax-modal_pending';
        var ajaxModal;
        var ajaxResponseList;
        var ajaxSessionCallbackList = {};
        var ajaxCount = 0;
        
        // Приватные методы
        //function privateFunction() {}

        // Конструктор
        function SingletonConstructor() {
            if (!instance) {
                instance = this;
            } else {
                return instance;
            }

            // Публичные свойства
            //instance.publicVar = '';

            // Публичные методы
            //instance.publicFunction = function() {};

            instance.addResponse = function(responseContent) {
                var responseContainer = $(responseContent);
                
                //Обрабатываем bootstrap формы
                responseContainer.find('[data-toggle="tooltip"]').bootstrapTooltip();
                responseContainer.find('[data-toggle="popover"]').bootstrapPopover();
                
                ajaxResponseList.append(responseContainer);
                
                return responseContainer;
            };
            instance.removeResponse = function(requestUid) {
                ajaxResponseList.find('#' + requestUid).remove();
            };

            instance.makeRequest = function(requestParams) {
                // Обрабатываем краткую запись, когда на входе только URL.
                if (typeof requestParams === 'string') {
                    requestParams = {url: requestParams};
                }
                
                $.extend(true, requestParams, {
                    beforeSend: function( xhr ) {
                        if (typeof requestParams.silent === 'undefined' || requestParams.silent === false) {
                            instance.ajaxStart();
                        }
                    }
                });
                $.ajax(requestParams).done(function ( data ) {
                    if (typeof requestParams.silent === 'undefined' || requestParams.silent === false) {
                        instance.ajaxStop();
                    }
                    if (typeof data === "object") {
                        // Если ответ в формате JSON...
                        if (typeof data.ajaxSessionUid !== 'undefined') {
                            // Если этот ответ закрывает ajax сессию, то вызываем отложенный callback и удаляем его.
                            ajaxSessionCallbackList[data.ajaxSessionUid](data);
                            delete ajaxSessionCallbackList[data.ajaxSessionUid];
                        } else {
                            // Если это обычный ajax ответ, то просто вызываем обработчик json ответа.
                            requestParams.jsonResponseHandler(data);
                        }
                    } else {
                        // Если ответ в формате HTML...
                        var responseContainer = instance.addResponse(data);
                        
                        if (typeof requestParams.htmlResponseHandler !== 'undefined') {
                            requestParams.htmlResponseHandler(responseContainer);
                        }
                    }
                }).fail(function(jqXHR, textStatus, errorThrown) {
                    if (typeof requestParams.silent === 'undefined' || requestParams.silent === false) {
                        instance.ajaxStop();
                    }
                    $.chwAlert().show(
                        "Произошла ошибка. Возможно у вас пропал Интернет. Попробуйте перезагрузить страницу. Если ошибка повторится, сообщите администратору сайта.",
                        $.chwAlert().TYPE_DANGER
                    );
                });
            };
            
            /**
             * Сохраняет jsonResponseHandler до тех пор, пока не будет
             * выдан ответ в JSON формате, содержащий ajaxSessionUid.
             * Это удобно при работе с ajax формами,
             * если при сохранении возникают ошибки, бекенд сам их обрабатывает,
             * как только все прошло хорошо, бекенд выдает JSON ответ, который
             * теперь уже должен обработать отложеный колбек полученный с фронтенда.
             */
            instance.makeSessionRequest = function(requestParams) {
                // Создаем UID ajax сессии.
                var ajaxSessionUid = Math.random();
                
                // В URL добавляем переменную ajaxSessionUid
                var prefix = '?';
                if (requestParams.url.indexOf("?") > -1) {
                    // Если есть символ "?", значит в URL есть параметры, меняем префикс.
                    prefix = '&';
                }
                requestParams.url += prefix + 'ajaxSessionUid=' + encodeURIComponent(ajaxSessionUid);
                
                /*
                 * jsonResponseHandler сохраняем в массиве до того момента,
                 * пока ответ не прийдет в формате JSON.
                 */
                ajaxSessionCallbackList[ajaxSessionUid] = requestParams.jsonResponseHandler;
                
                instance.makeRequest(requestParams);
            };
            
            instance.ajaxStart = function(){
                var modal = $('.' + ajaxModalClass);
                if (modal.length === 0) {
                    $(doc.body).append('<div class="' + ajaxModalClass + '">');
                }
                if(!ajaxCount)
                    ajaxModal.addClass(ajaxPendingClass);
                ajaxCount++;
            };
	
            instance.ajaxStop = function(){
                ajaxCount--;
                if(!ajaxCount)
                    ajaxModal.removeClass(ajaxPendingClass);
            };
            
            ajaxResponseList = $('<div>');
            ajaxResponseList.addClass(ajaxResponseListClass);
            $(doc.body).append(ajaxResponseList);
            
            ajaxModal = $('<div>');
            ajaxModal.addClass(ajaxModalClass);
            $(doc.body).append(ajaxModal);
        }

        return SingletonConstructor;
    })();

    $.chwAjax = function(opts) {
        return new ChwAjax();
    };

})($, document, window);