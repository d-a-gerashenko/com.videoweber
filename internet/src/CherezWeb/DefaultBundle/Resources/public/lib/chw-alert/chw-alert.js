;(function($, doc, win) {
    "use strict";
    var ChwAlert = (function() {
        var instance;

        // Приватные свойства
        //var privateVar = '';
        var alerts = {};
        var currentAlertNum = 0;
        var alertListClass = 'chw-alert-alerts';
        var alertList;
        var alertContainerClass = 'chw-alert-alerts__item';

        // Приватные методы
        //function privateFunction() {}
        function getAlertTypeClass(alertType) {
            if (typeof alertType === 'undefined') {
                alertType = instance.TYPE_SUCCSESS;
            }
            return alertContainerClass + '_' + alertType;
        }
        function getAlertId(alertNum) {
            return alertContainerClass + '-' + alertNum;
        }

        // Конструктор
        function SingletonConstructor() {
            if (!instance) {
                instance = this;
            } else {
                return instance;
            }

            // Публичные свойства
            //instance.publicVar = '';
            instance.TYPE_SUCCSESS = 'success';
            instance.TYPE_WARNING = 'warning';
            instance.TYPE_DANGER = 'danger';
            instance.TYPE_INFO = 'info';

            // Публичные методы
            //instance.publicFunction = function() {};
            instance.show = function(alertContent, alertType) {
                var alertNum = currentAlertNum;
                currentAlertNum++;
                
                //Контейнер сообщений не отображается на странице, когда нет сообщений.
                alertList = $('.' + alertListClass);
                if(typeof alertList[0] === 'undefined') {
                    alertList = $('<div>').addClass(alertListClass);
                    $(doc.body).append(alertList);
                }
                
                var alertTypeClass = getAlertTypeClass(alertType);
                var alertId = getAlertId(alertNum);
                
                // Интегрируем с BootStrap
                var alertContainer = $('\
                    <div class="alert alert-' + alertType + ' alert-dismissible ' + alertContainerClass + ' ' + alertTypeClass + '" role="alert" id="' + alertId + '">\
                        <button type="button" class="close" data-dismiss="alert"><span aria-hidden="true">&times;</span><span class="sr-only">Закрыть</span></button>\
                        ' + alertContent + '\
                    </div>\
                ');
                alertContainer.on('closed.bs.alert', function () {
                    instance.remove(alertNum);
                });
        
                alerts[alertNum] = {
                    alertContent: alertContent,
                    alertContainer: alertContainer,
                    alertType: alertType,
                    id: alertId
                };
                
                alertList.append(alertContainer);
                
                instance.removeWithDelay(alertNum);
            };
            
            instance.remove = function(alertNum) {
                var alertToremove = alerts[alertNum];
                if (typeof alertToremove !== 'undefined') {
                    alertToremove.alertContainer.hide('slow', function(){
                        $(this).remove();
                        delete(alerts[alertNum]);
                        if(Object.keys(alerts).length === 0) {
                            alertList.remove();
                        }
                    });
                }
            };
            
            instance.removeWithDelay = function(alertNum){
                //Задержка в миллисекундах, 1000ms = 1s
                setTimeout(function () {instance.remove(alertNum);},10000);
            };
        }

        return SingletonConstructor;
    })();
    
    $.chwAlert = function(opts) {
        return new ChwAlert();
    };
})($, document, window);