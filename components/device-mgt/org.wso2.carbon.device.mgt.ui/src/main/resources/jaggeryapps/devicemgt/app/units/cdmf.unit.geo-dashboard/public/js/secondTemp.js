
    function reformatRadius(val){
        if(val != "" && !isNaN(val)){
            $("#fRadius" ).val(parseFloat(Math.round(val * 100) / 100).toFixed(2));
        } else{
            var message = "Invalid Fluctuation Radius Provided.";
            noty({text: message, type: 'error'});
        }
    }

