requirejs.config({
    baseUrl: '/assets/js',
    paths: {
        lib: '/libs',
        react: '/libs/react/15.0.1/dist/react.min',
        reactdom: '/libs/react-dom/15.0.1/dist/react-dom.min',
        immutable: '/libs/immutable/3.8.1/dist/immutable.min'
    }
});

requirejs(
[
    "react",
    "reactdom",
    "components/status-table"
],
function(
    React,
    ReactDOM,
    StatusTable
)
{
    ReactDOM.render(
        <StatusTable.StatusTable />,
        document.getElementById('status-table-container')
    );
});

