/*
 * particles.js config
 * This file is loaded by login.html
 */
particlesJS('particles-js', {
    "particles": {
        "number": {
            "value": 150, // Increased from 100
            "density": {
                "enable": true,
                "value_area": 800
            }
        },
        "color": {
            "value": "#5a639c"
        },
        "shape": {
            "type": "circle",
        },
        "opacity": {
            "value": 0.6, // Increased from 0.5
            "random": true,
        },
        "size": {
            "value": 3,
            "random": true,
        },
        "line_linked": {
            "enable": true,
            "distance": 150,
            "color": "#5a639c",
            "opacity": 0.5, // Increased from 0.4
            "width": 1
        },
        "move": {
            "enable": true,
            "speed": 4, // Increased from 3
            "direction": "none",
            "random": false,
            "straight": false,
            "out_mode": "out",
            "bounce": false,
        }
    },
    "interactivity": {
        "detect_on": "canvas",
        "events": {
            "onhover": {
                "enable": true,
                "mode": "repulse"
            },
            "onclick": {
                "enable": true,
                "mode": "push"
            },
            "resize": true
        },
        "modes": {
            "repulse": {
                "distance": 100,
                "duration": 0.4
            },
            "push": {
                "particles_nb": 4
            },
        }
    },
    "retina_detect": true
});