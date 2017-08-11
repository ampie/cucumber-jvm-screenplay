when 'To Do', {
    'success' should: ['Done']
    'failure' should: ['In Progress']
    'error' should: ['In Progress']
}
when 'Resolved', {
    'failure' should: ['To Do']
    'error' should: ['To Do']
    'pending' should: ['To Do']
}

when 'In Progress', {
    'success' should: ['Done']
}

when 'Closed', {
    'failure' should: ['To Do']
    'error' should: ['To Do']
    'pending' should: ['To Do']
}

when 'Done', {
    'failure' should: ['To Do']
    'error' should: ['To Do']
    'pending' should: ['To Do']
}