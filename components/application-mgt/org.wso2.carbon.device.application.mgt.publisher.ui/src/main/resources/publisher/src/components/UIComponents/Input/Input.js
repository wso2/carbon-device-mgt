import React, {Component} from 'react';
import './input.css';

class Input2 extends Component {

    render() {
        return (
            <div className="custom-input">
                <input type="text" required/>
                <span className="highlight"></span>
                <span className="under-line"></span>
                <label>Name</label>
            </div>
        )
    }

}

export default Input2;
