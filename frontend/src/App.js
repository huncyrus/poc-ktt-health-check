import React, {Component, useEffect, useState, setState } from 'react';
import 'bulma';
import './App.css';
import './components/AddServiceComponent'
import AddServiceComponent from './components/AddServiceComponent';
import ListServiceComponent from './components/ListServiceComponent';
import StatusComponent from './components/StatusComponent';
import fetchData from './libs/fetchData';

/**
 * Central component
 */
class App extends Component {
    state = {
        loadingStatus: false
        , listOfServices: []
        , statusMessage: ''
        , activeView: 'overview' // overview, add, update
    }

    constructor (props) {
        super(props);
    }

    /**
     * Empty the statuses at unmount event.
     */
    componentWillUnmount = () => {
        this.setState({loadingStatus: false, listOfServices: [], statusMessage: ''});
    }

    /**
     * Fetch data & start timer to re-check an API for information.
     */
    componentDidMount = () => {
        let that = this;
        //this._testing();

        try {
            fetchData
                .getAvailableServicesForUser()
                .then(data => {
                    console.log('retrieved data from server: ', data);
                    that.setState({listOfServices: data});
                    that.setState({statusMessage: "List retrieved."});
                })
                .catch(err => {
                    console.error('[cdm] data not retrieved... -> ', err);
                    that.setState({statusMessage: "Error at retriving the list: " + err});
                })
            ;
        } catch (err) {
            console.error('Error at retrieving data w/ fetch: ', err);
        }

        try {
            that.setState({statusMessage: "Attempt to retrieve service list..."});

            setInterval(async () => {
                console.log('Attempt to retriving data....');
                fetchData.getAvailableServicesForUser()
                    .then(data => {
                        console.log('retrieved data from server: ', data);
                        that.setState({listOfServices: data});
                    })
                    .catch(err => {
                        console.error('[cdm] data not retrieved... -> ', err);
                    })
            }, 10000); // ms!
        } catch (err) {
            console.error('Error at retrieving data: ', err);
        }
    }

    /**
     * Retrieving information for the user
     */
    getInitialDataForUser () {
        let that = this;
        that.setState({loadingStatus: true});

        fetchData.getInitialDataForUser()
            .then(data => {
                that.setState({listOfServices: data});
            })
            .catch(err => {
                that.setState({statusMessage: err});
            })
            .finally ( () => {
                that.setState({loadingStatus: false});
            })
        ;
    }

    /**
     * Handling the remove event from the list. Shall trigger a database remove API endpoint.
     * This method shall update the status message for the user.
     *
     * @param {integer} id 
     */
    handleRemove = (id) => {
        console.log('remove handle for id: ' + id);
        let that = this;

        fetchData.removeOneService(id)
            .then (res => {

            })
            .catch (err => {
                that.setState({statusMessage: "Error at remove: " + err});
            });
    }

    /**
     * This method shall handle payload from the frontend (through event.target) and send it to the backend
     * through HTTP Post to update a data, then trigger a data refresh query.
     *
     * @param {Integer} id 
     * @todo implement it later
     */
    handleUpdate = (id) => {
        console.log('update handle for id: ' + id);
    }

    /**
     * Simple state (view) changer event handler.
     */
    handleAddNewButton = () => {
        this.setState({activeView: 'add'});
    }

    /**
     * Handle save new service button
     * Event handler from the add new entry view to gather information and send through HTTP(s) to the 
     * backend and let it save. 
     * This method shall send a signal message to the user through the statusMessage.
     *
     * @param {Object} data 
     */
    handleSaveNewServiceButton = (data) => {
        console.log('saved data: ', data);
        this.setState({activeView: 'overview'});
        this.setState({statusMessage: "New service saved."});
        fetchData.addNewService(data.service_address, data.service_name)
            .then(data => {
                this.setState({statusMessage: "Data saved."});
            })
            .catch(err => {
                this.setState({statusMessage: "Data not saved."});
            })
        ;
    }

    /**
     * Close status message info box event handler
     */
    handleCloseStatusMessage = () => {
        this.setState({statusMessage: ""});
    }


    _testing () {
        let x = [
            {"id":6,"service_url":"https://nemletezem55555.com","service_name":"false pozitiv teszt","cr_date":"2021-02-14T12:03:24","mod_date":"2021-02-14T12:03:24","service_enabled":1,"response_time":20, "service_status": 0},
            {"id":5,"service_url":"https://www.index.hu","service_name":"fifth_name","cr_date":"2021-02-13T19:14:21","mod_date":"2021-02-13T19:14:21","service_enabled":1,"response_time":541, "service_status": 1},
            {"id":4,"service_url":"https://www.nemletezik23343.hu","service_name":"fourth_name","cr_date":"2021-02-13T19:14:09","mod_date":"2021-02-13T19:14:09","service_enabled":1,"response_time":16, "service_status": 0},
            {"id":3,"service_url":"https://www.google.com","service_name":"thrid_name","cr_date":"2021-02-13T19:13:55","mod_date":"2021-02-13T19:13:55","service_enabled":1,"response_time":81, "service_status": 1},
            {"id":2,"service_url":"https://www.facebook.com","service_name":"second_name","cr_date":"2021-02-13T19:13:43","mod_date":"2021-02-13T19:13:43","service_enabled":1,"response_time":234, "service_status": 1},
            {"id":1,"service_url":"https://www.kry.se","service_name":"test name","cr_date":"2021-02-10T21:26:14","mod_date":"2021-02-10T21:26:14","service_enabled":1,"response_time":135, "service_status": 1}
        ];

        this.setState({listOfServices: x});
        console.log('the state of services: ', this.state.listOfServices);
    }

    /**
     * Core render.
     */
    render () {
        console.log('hello', this.state.listOfServices);

        let { listOfServices, activeView, statusMessage } = this.state;

        return (
            <div className="App">
                <header>
                    <h1 className="title is-1">KTT service health monitor</h1>
                </header>
                <section className="section">
                    <StatusComponent
                        statusMessage = {statusMessage}
                        handleCloseStatusMessage = {this.handleCloseStatusMessage}
                    />

                    {'add' === activeView ? (
                        <AddServiceComponent 
                            handleSave = {this.handleSaveNewServiceButton}
                        />
                    ) : (
                        <ListServiceComponent 
                            listOfServices = {listOfServices} 
                            handleUpdate = {this.handleUpdate}
                            handleRemove = {this.handleRemove}
                            handleAddNew = {this.handleAddNewButton}
                        />
                    )}
                </section>
            </div>
        );
    }
}

export default App;
