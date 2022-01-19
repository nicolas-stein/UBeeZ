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

    var ubidotsRequestBody;

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

        var ubidotsRequestJSON = {}
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
                ubidotsRequestJSON["position"] = {value:valueOrient, context:{lat:valueLat, lng:valueLng}}
            }

            //ubidotsRequestJSON["Orientation"] = payload.readUIntBE(4, 2) >> 7;
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
                ubidotsRequestJSON["humidite0"] =  value * 100 / 64;
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
                ubidotsRequestJSON["humidite1"] =  value * 100 / 64;
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
                ubidotsRequestJSON["temperature0"] =  value / 10 - 20;
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
                ubidotsRequestJSON["temperature1"] =  value / 10 - 20;
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
                ubidotsRequestJSON["temperature2"] =  value / 10 - 20;
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
                ubidotsRequestJSON["temperature3"] =  value / 10 - 20;
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
                ubidotsRequestJSON["poid"] =  value / 10;
            }

            //Batterie
            value = (payload.readUIntBE(7, 2) & 0x03FE) >> 1;
            if(value == 511){
                //TODO : batterie out of range
            }
            else{
                ubidotsRequestJSON["batterie"] =  value / 100;
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
                ubidotsRequestJSON["luminosite"] =  11.019*Math.exp(0.0069*value);
            }
        }

        ubidotsRequestBody = JSON.stringify(ubidotsRequestJSON);
    }
    else{
        ubidotsRequestBody = JSON.stringify(azureRequestJSON);
    }



    const ubidotsRequestOptions = {
        hostname: 'things.ubidots.com',
        port: 443,
        path: '/api/v1.6/devices/<YOUR_DEVICE_HERE>/',
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
            'Content-Length': ubidotsRequestBody.length,
            'X-Auth-Token': '<YOUR_AUTH_TOKEN_HERE>'
        }
    }

    var ubidotsResponse = '';
    const ubidotsRequest = http.request(ubidotsRequestOptions, (res) => {
        context.log(`statusCode: ${res.statusCode}`)

        res.on('data', (d) => {
            ubidotsResponse += d;
        })

        res.on('end', (d) => {
            context.res = {
                body: ubidotsResponse,
                statusCode : res.statusCode
            }
           context.done();
        })
    })

    ubidotsRequest.on('error', (error) => {
        context.log.error(error)
        context.done();
    })

    ubidotsRequest.write(ubidotsRequestBody);
    ubidotsRequest.end();
};