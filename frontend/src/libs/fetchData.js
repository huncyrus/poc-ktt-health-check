// fetchData.js
import 'validator';
import validator from 'validator';

/**
 * Fetch Data
 * Simple HTTP client implementation. The entire class is promise based.
 * 
 * @author Györk Bakonyi <huncyrus@gmail.com>
 * @version 1.1
 */
class fetchData {
    constructor () {
        //
    }

    /**
     * Get all enabled service as list (JSON)
     * @param {Integer} userId 
     * @return {Promise} json object
     */
    static getAvailableServicesForUser (userId = 1) {
        let url = "http://localhost:8089/api/all";

        return this._apiQuery(url);
    }

    static removeOneService (serviceId, userId = 1) {
        let url = "http://localhost:8089/api/ " + serviceId + "/remove/";

        return this._apiQuery(url);
    }

    static addNewService (address, name, userId = 1) {
        let url = "http://localhost:8089/api/add";

        return this._apiQuery(url, {server_address: address, server_name: name, userId: userId});
    }

    /**
     * Private method to handle minimalistic validation and requests (GET & POST only).
     *
     * @param {String} url 
     * @param {Object} input 
     * @return {Promise}
     */
    static _apiQuery (url, input = null) {
        let that = this;

        return new Promise((resolve, reject) => {
            if (!url) {
                reject("URL does not present");
            }

            validator.whitelist("http://localhost");
            if (!validator.isWhitelisted && !validator.isURL(url)) {
                reject("URL (" + url + ") is not valid");
            }

            if (null === input) {
                this
                    ._RequestGet(url)
                    .then(data => resolve(data))
                    .catch(err => reject(err))
                ;
            } else {
                this
                    ._RequestPost(url, input)
                    .then(data => resolve(data))
                    .catch(err => reject(err))
                ;
            }            
        });
        
    }

    static _RequestGet (url) {
        
        return new Promise((resolve, reject) => {
            fetch(url, {method: 'get', headers: {'Access-Control-Allow-Origin': '*'}})
                    .then(res => res.json())
                    .then(data => {
                        console.log('retrieved info: ', data);
                        resolve(data);
                    })
                    .catch(err => {
                        reject(err);
                    })
                ;
        });
    }

    static _RequestPost (url, payload) {
        return new Promise ((resolve, reject) => {
            fetch(url, 
                {
                    method: 'post'
                    , headers: {
                        'Accept': 'application/json, text/plain, */*',
                        'Content-Type': 'application/json',
                        'Access-Control-Allow-Origin': '*'
                    },
                    body: payload
                }
            )
                .then(res => res.json())
                .then(data => {
                    resolve(data);
                })
                .catch(err => {
                    reject(err);
                })
            ;
        });
    }
}

export default fetchData;
