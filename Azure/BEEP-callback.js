var http = require('https');
module.exports = function (context, req) {

    try{
        var azureRequestJSON = JSON.parse(req.rawBody)
    }
    catch(e){
        context.res = {
                body: "Error 400 : "+e,
                statusCode : 400
            }
           context.done();
           return;
    }

    if(azureRequestJSON==null){
        context.res = {
                body: "Error 400 : empty request body",
                statusCode : 400
            }
           context.done();
           return;
    }

    var beepRequestBody;

    if(azureRequestJSON.hasOwnProperty("message")){
        //TODO
        payload = Buffer.from(azureRequestJSON["message"], 'hex');
        if(payload.length!=12){
            context.res = {
                body: "Error 400 : message length is not 12 bytes",
                statusCode : 400
            }
           context.done();
           return;
        }

        var beepRequestJSON = {}
        beepRequestJSON["key"] = "<YOUR_BEEP_KEY_HERE>";

        if(payload.readUIntBE(11, 1) & 0x01 > 0){
            //TODO : Is GPS
            context.log("Payload is GPS");
            var valueLat;
            var valueLng;
            var valueOrient;

            valueLat = (payload.readUIntBE(0, 3) >> 5);
            valueLng = (payload.readUIntBE(2, 3) & 0x1FFFFC)>> 2;
            valueOrient = (payload.readUIntBE(4, 2) & 0x03FE)>> 1;

            if(valueLat == 524287 || valueLng == 524287 || valueOrient == 511){
                //TODO : latitude error
            }
            else if(valueLat == 524286 || valueLng == 524286 || valueOrient > 360){
                //TODO : latitude out of range
            }
            else{
                valueLat = valueLat/1000 - 90;
                valueLng = valueLng/1000 - 180;
                beepRequestJSON["lat"] = valueLat;
                beepRequestJSON["lon"] = valueLng;
            }

            //beepRequestJSON["Orientation"] = payload.readUIntBE(4, 2) >> 7;
        }
        else{
            //Is not GPS
            context.log("Payload is not GPS");
            var value;

            //Humidite 0
            value = (payload.readUIntBE(0, 1) >> 2);
            if(value == 63){
                //TODO : humidite0 error
            }
            else if(value == 62){
                //TODO : humidite0 out of range
            }
            else{
                beepRequestJSON["h_i"] =  value * 100 / 64;
            }

            //Humidite 1
            value = (payload.readUIntBE(0, 2) & 0x03F0) >> 4;
            if(value == 63){
                //TODO : humidite1 error
            }
            else if(value == 62){
                //TODO : humidite1 out of range
            }
            else{
                beepRequestJSON["h"] =  value * 100 / 64;
            }

            //Temperature 0
            value = (payload.readUIntBE(1, 2) & 0x0FFC) >> 2;
            if(value == 1023){
                //TODO : temperature0 error
            }
            else if(value == 1022){
                //TODO : temperature0 out of range
            }
            else{
                beepRequestJSON["t_i"] =  value / 10 - 20;
            }

            //Temperature 1
            value = payload.readUIntBE(2, 2) & 0x03FF;
            if(value == 1023){
                //TODO : temperature1 error
            }
            else if(value == 1022){
                //TODO : temperature1 out of range
            }
            else{
                beepRequestJSON["t_1"] =  value / 10 - 20;
            }

            //Temperature 2
            value = (payload.readUIntBE(4, 2) & 0xFFC0) >> 6;
            if(value == 1023){
                //TODO : temperature2 error
            }
            else if(value == 1022){
                //TODO : temperature2 out of range
            }
            else{
                beepRequestJSON["t_2"] =  value / 10 - 20;
            }

            //Temperature 3
            value = (payload.readUIntBE(5, 2) & 0x3FF0) >> 4;
            if(value == 1023){
                //TODO : temperature3 error
            }
            else if(value == 1022){
                //TODO : temperature3 out of range
            }
            else{
                beepRequestJSON["t"] =  value / 10 - 20;
            }

            //Poid
            value = (payload.readUIntBE(6, 2) & 0x0FFC) >> 2;
            if(value == 1023){
                //TODO : poid error
            }
            else if(value == 1022){
                //TODO : poid out of range
            }
            else{
                beepRequestJSON["weight_kg"] =  value / 10;
            }

            //Batterie
            value = (payload.readUIntBE(7, 2) & 0x03FE) >> 1;
            if(value == 511){
                //TODO : batterie out of range
            }
            else{
                beepRequestJSON["bv"] =  value / 100;
            }

            //Luminosité
            value = (payload.readUIntBE(8, 3) & 0x01FF80) >> 7;
            if(value == 1023){
                //TODO : luminosite error
            }
            else if(value == 1022){
                //TODO : luminosite out of range
            }
            else{
                beepRequestJSON["l"] =  11.019*Math.exp(0.0069*value);
            }
        }

        beepRequestBody = JSON.stringify(beepRequestJSON);
    }
    else{
        beepRequestBody = JSON.stringify(azureRequestJSON);
    }



    const beepRequestOptions = {
        hostname: 'api.beep.nl',
        port: 443,
        path: '/api/sensors',
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
            'Content-Length': beepRequestBody.length
        }
    }

    var beepResponse = '';
    const beepRequest = http.request(beepRequestOptions, (res) => {
        context.log(`statusCode: ${res.statusCode}`)

        res.on('data', (d) => {
            beepResponse += d;
        })

        res.on('end', (d) => {
            context.res = {
                body: beepResponse,
                statusCode : res.statusCode
            }
           context.done();
        })
    })

    beepRequest.on('error', (error) => {
        context.log.error(error)
        context.done();
    })

    beepRequest.write(beepRequestBody);
    beepRequest.end();
};