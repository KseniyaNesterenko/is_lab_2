function formatNumber(number) {
    if (typeof number === 'number' && !isNaN(number)) {
        if (number > 1e15) {
            return number.toFixed(0).replace(/\B(?=(\d{3})+(?!\d))/g, '') + '...';
        } else {
            return number.toFixed(0).replace(/\B(?=(\d{3})+(?!\d))/g, '');
        }
    }
    return number;
}

function formatTableNumbers() {
    var cells = document.querySelectorAll('.number-cell');
    cells.forEach(function(cell) {
        var value = cell.innerText.trim();
        if (value) {
            var number = parseFloat(value);
            if (!isNaN(number)) {
                cell.innerText = formatNumber(number);
            }
        }
    });
}

window.onload = formatTableNumbers;



