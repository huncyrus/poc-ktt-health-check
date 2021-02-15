import React, {Component} from 'react';
import CheckCircleIcon from '@material-ui/icons/CheckCircle';
import DnsIcon from '@material-ui/icons/Dns';

/**
 * Add service component
 * Simple component (view state) to let a user add new service
 */
class AddServiceComponent extends Component {
    constructor (props) {
        super(props);
        this.initialState = {
            service_address: '',
            service_name: ''
        };

        this.state = this.initialState;
    }

    /**
     * On Form Submit
     * Simple event subscriber/handler to change state when user click on submit button or the form is sent.
     * This method shall trigger the parent handleSave method to let the core save the newly introduced information.
     *
     * @param {Object} event 
     */
    onFormSubmit = (event) => {
        event.preventDefault();
        this.props.handleSave(this.state);
        this.setState(this.initialState);
    }

    /**
     * Dedicated method to update the address state
     * @param {Object} event 
     */
    updateChangedAddress = (event) => {
        this.setState({service_address: event.target.value});
    }
    /**
     * Dedicated method to update the name state
     * @param {Object} event 
     */
    updateChangedName = (event) => {
        this.setState({service_name: event.target.value});
    }


    render () {
        let { service_address, service_name } = this.state;

        return (
            <div className="container">
                <h1 className="title is-2">
                    Add new service
                </h1>
                <form
                    onSubmit={this.onFormSubmit}
                >
                    <div className="container">
                        <div className="field is-horizontal">
                            <div className="field-label is-normal">
                                <label className="label">URL</label>
                            </div>
                            <div className="field-body">
                                <div className="field">
                                    <p className="control is-expanded">
                                        <input 
                                            id = "service_address"
                                            className="input" 
                                            name="service_address" 
                                            type="text" 
                                            placeholder="Service address, URL" 
                                            onChange={this.updateChangedAddress}
                                            value={service_address}
                                        />
                                    </p>
                                </div>
                            </div>
                        </div>
                        <div className="field is-horizontal">
                            <div className="field-label is-normal">
                                <label className="label">Name</label>
                            </div>
                            <div className="field-body">
                                <div className="field">
                                    <p className="control is-expanded has-icons-left">
                                        <input 
                                            id="service_name"
                                            className="input" 
                                            name="service_name" 
                                            type="text" 
                                            placeholder="Service name" 
                                            onChange={this.updateChangedName}
                                            value={service_name}
                                        />
                                        <span className="icon is-small is-left">
                                            <i className="fas fa-network-wired"></i>
                                        </span>
                                    </p>
                                </div>
                            </div>
                        </div>
                        <div className="is-horizontal is-center is-centered has-text-centered">
                            
                            <input type="submit" className="button is-primary" value="Save service" />
                            
                        </div>
                    </div>
                </form>
            </div>
        )
    }
}

export default AddServiceComponent;

