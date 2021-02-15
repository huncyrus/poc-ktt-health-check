import React, {Component} from 'react';
import CheckCircleIcon from '@material-ui/icons/CheckCircle';
import DnsIcon from '@material-ui/icons/Dns';
import InfoIcon from '@material-ui/icons/Info';

class StatusComponent extends Component {
    constructor (props) {
        super(props);
    }

    render () {
        let {statusMessage} = this.props;
        return (
            <div className="container">
                {
                    typeof statusMessage === 'undefined'
                    || '' === statusMessage
                    ? (
                        <div></div>
                    ) : (
                        <div className="is-center is-centeres has-text-centered">
                            <article className="message is-info">
                                <div className="message-header">
                                <p>
                                    <InfoIcon />
                                    Status Info
                                </p>
                                <button 
                                    className="delete" 
                                    aria-label="delete"
                                    onClick={(e) => this.props.handleCloseStatusMessage()}
                                ></button>
                                </div>
                                <div className="message-body">
                                    {statusMessage}
                                </div>
                            </article>
                            
                        </div>
                    )
                }
            </div>
            
        );
    }
}

export default StatusComponent;
