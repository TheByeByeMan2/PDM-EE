import * as React from 'react'
import { createRoot } from 'react-dom/client'
import {App} from './App'

const page = document.getElementById("the-page")

const root = createRoot(page)

root.render(
    <App />
)