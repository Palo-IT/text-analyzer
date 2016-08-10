d3.json('http://localhost:8080/words/30', function (error, words) {
    createTable(words.sort(function (o1, o2) {
        return o1.size >= o2.size ? -1 : 1;
    }));
    drawCloud(words);
});

function drawCloud(words) {
    var colors = d3.scale.category20();
    var color = d3.scale.linear()
        .domain([0, 1, 2, 3, 4, 5, 6, 10, 15, 20, 100])
        .range(colors.range());
    var minSize = d3.min(words, function (d) {
        return d.size;
    });
    var maxSize = d3.max(words, function (d) {
        return d.size;
    });
    var size = d3.scale.linear()
        .domain([minSize, maxSize])
        .range([10, 60]);

    var rotation = d3.scale.linear()
        .domain([0.0, 1.0])
        .range([-45, 45]);

    d3.layout.cloud().size([900, 500])
        .words(words)
        .rotate(function (d) {
            return rotation(Math.random());
        })
        .fontSize(function (d) {
            return size(d.size);
        })
        .on("end", draw)
        .start();

    function draw(data) {
        d3.select("body").insert("svg", ":first-child")
            .attr("width", 900)
            .attr("height", 500)
            .attr("class", "wordcloud")
            .append("g")
            // without the transform, words words would get cutoff to the left and top, they would
            // appear outside of the SVG area
            .attr("transform", "translate(320,200)")
            .selectAll("text")
            .data(data)
            .enter().append("text")
            .style("font-size", function (d) {
                return d.size + "px";
            })
            .style("fill", function (d, i) {
                return color(i);
            })
            .attr("transform", function (d) {
                return "translate(" + [d.x, d.y] + ")rotate(" + d.rotate + ")";
            })
            .text(function (d) {
                return d.text;
            })
            .on('mouseenter', function (d) {
                d3.select(this).classed("active", true);
            })
            .on('mouseleave', function (d) {
                d3.select(this).classed("active", false);
            });
    }
}

function createTable(data) {
    var thead = d3.select("thead").selectAll("th")
        .data(d3.keys(data[0]))
        .enter().append("th").text(function (d) {
            return d;
        });
// fill the table
// create rows
    var tr = d3.select("tbody")
        .selectAll("tr")
        .data(data).enter().append("tr");
// cells
    var td = tr.selectAll("td")
        .data(function (d) {
            return d3.values(d)
        })
        .enter().append("td")
        .text(function (d) {
            return d
        })

}