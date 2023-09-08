import * as React from "react"
import { createElement } from "react"
import { Rule } from "./Rule"


export const exempleRule: Array<Rule> = [
    {
        ruleId: 1,
        gridSize: '10x10',
        shotNumber: 1,
        timeout: 3000
    },
    {
        ruleId: 2,
        gridSize: '10x10',
        shotNumber: 1,
        timeout: 3150
    },
    {
        ruleId: 3,
        gridSize: '10x10',
        shotNumber: 1,
        timeout: 3350
    },
]

export function buildRuleOptions(rules: Array<Rule>|undefined, rule: React.MutableRefObject<HTMLSelectElement>, formValue, name, value) {
    function ruleName(ruleId) {
        if (ruleId == 1) return 'Default Rule'
        if (ruleId == 2) return 'Tets Rule'
        else return `Custome Rule ${ruleId - 2}`
    }
    if (!rules) return undefined
    else return createElement(
        'select', {
        ref: rule,
        onChange: formValue,
        name: name,
        value: value
    }, rules.map(rule =>
        createElement("option", { value: rule.ruleId }, ruleName(rule.ruleId))
    )
    )
}

export function buildRuleInfo(rule: Rule|undefined) {
    if (!rule) return (<div>... is loading</div>)
    else return (
        <div>
            <tr>grid Size = {rule.gridSize}</tr>
            <tr>shot Number = {rule.shotNumber}</tr>
            <tr>timeout = {rule.timeout}</tr>
        </div>
    )
}