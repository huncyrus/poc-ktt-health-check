import React, { Component } from 'react';

import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";
import '@fortawesome/fontawesome-free';

import AddCircleIcon from '@material-ui/icons/AddCircle';
import RemoveCircleIcon from '@material-ui/icons/RemoveCircle';
import BuildIcon from '@material-ui/icons/Build';

/**
 * List service component
 * Simple table list component to display and render JSON based incoming information
 * (JSON information is stored in state in the parent/caller component (e.g.: App.js))
 * 
 * @note the sturcture should be splitted even smaller parts, like table head, table body and rows. But due lack of time this is just one big component for now.
 */
class ListServiceComponent extends Component {
    constructor (props) {
        super(props);
    }

    render () {
        let { listOfServices } = this.props;

        return (
            <div className="container">
                <h2 className="title is-2">List of services</h2>
                <div className="container">
                    <div className="is-right has-text-right">
                        <button 
                            className="button is-success has-icon"
                            onClick={(e) => this.props.handleAddNew()}
                        >
                            <AddCircleIcon />
                            Add new
                        </button>
                    </div>
                    <div className="table-container">
                        <table className="table is-striped is-hoverable">
                            <thead>
                                <tr>
                                    <th><abbr title="Service name">Service Name</abbr></th>
                                    <th>Service Address</th>
                                    <th>Available</th>
                                    <th>Response time (ms)</th>
                                    <th>Options</th>
                                </tr>
                            </thead>
                            {
                                typeof listOfServices === 'undefined'
                                || null === listOfServices
                                || '' === listOfServices
                                || 0 === listOfServices.length
                                ?
                                (
                                    <tbody><tr><td>No available service.</td></tr></tbody>
                                ) : (
                                    <tbody>
                                    {listOfServices.map((item) => (
                                        <tr key={item.id}>
                                            <td>{item.service_name}</td>
                                            <td>{item.service_url}</td>
                                            <td>{item.service_status}</td>
                                            <td>{item.response_time}</td>
                                            <td>
                                            <button 
                                                name="update" 
                                                className="button is-info has-icons-left"
                                                onClick={(e) => this.props.handleUpdate(item.id)}
                                            >
                                                <BuildIcon />
                                                Update/Edit
                                            </button>
                                            &nbsp; 
                                            <button 
                                                name="remove" 
                                                className="button is-danger has-icons-left"
                                                onClick={(e) => this.props.handleRemove(item.id)}
                                            >
                                                <RemoveCircleIcon />
                                                Remove
                                            </button>
                                            </td>
                                        </tr>
                                    ))}
                                    </tbody>
                                    
                                )
                            }
                        </table>
                    </div>
                </div>
            </div>
        );
    }
}

export default ListServiceComponent;
