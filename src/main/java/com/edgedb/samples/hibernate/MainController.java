package com.edgedb.samples.hibernate;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import com.edgedb.samples.hibernate.carrot.Carrot;
import com.edgedb.samples.hibernate.carrot.CarrotRepo;
import com.edgedb.samples.hibernate.figure.Figure;
import com.edgedb.samples.hibernate.figure.FigureRepo;
import com.edgedb.samples.hibernate.fox.Fox;
import com.edgedb.samples.hibernate.fox.FoxRepo;
import com.edgedb.samples.hibernate.position.Position;
import com.edgedb.samples.hibernate.position.PositionRepo;
import com.edgedb.samples.hibernate.rabbit.Rabbit;
import com.edgedb.samples.hibernate.rabbit.RabbitRepo;
import jakarta.transaction.Transactional;


@Controller
public class MainController {

    @Autowired
    FigureRepo figureRepo;

    @Autowired
    RabbitRepo rabbitRepo;

    @Autowired
    CarrotRepo carrotRepo;

    @Autowired
    FoxRepo foxRepo;

    @Autowired
    PositionRepo positionRepo;

    @Autowired
    SpawnService spawnService;

    @GetMapping("/")
    public String home(@RequestParam(name = "x", required = false) Integer x,
            @RequestParam(name = "y", required = false) Integer y, Model model) {
        var figures = figureRepo.findAll();
        var carrots = carrotRepo.findAll();

        var centerX = x == null ? 0 : (int) x;
        var centerY = y == null ? 0 : (int) y;
        var center = new CellPos(centerX, centerY);
        var cells = this.renderCells(center, figures, carrots);
        model.addAttribute("cells", cells);

        var figuresLeaderboard = figureRepo.findTop10ByPositionIsNotNullOrderByScoreDescName();
        model.addAttribute("leaderboard", figuresLeaderboard);
        return "home";
    }

    @GetMapping("/figures/{id}")
    public ModelAndView viewFigure(@PathVariable UUID id, Model model) {
        var figure = figureRepo.findById(id);
        if (figure == null) {
            return new ModelAndView("redirect:/");
        }
        if (figure.getPosition() != null) {
            var pos = figure.getPosition();
            return new ModelAndView("redirect:/?x=" + pos.getX() + "&y=" + pos.getY());
        }

        model.addAttribute("figure", figure);
        
        var figuresLeaderboard = figureRepo.findTop10ByPositionIsNotNullOrderByScoreDescName();
        model.addAttribute("leaderboard", figuresLeaderboard);

        return new ModelAndView("figure-dead");
    }

    @GetMapping("/figures/{id}/move")
    public ModelAndView moveFigure(@PathVariable UUID id,
            @RequestParam(name = "dx", required = false) Integer dx,
            @RequestParam(name = "dy", required = false) Integer dy) {
        var figure = figureRepo.findById(id);
        if (figure == null) {
            return new ModelAndView("redirect:/");
        }
        if (figure.getPosition() == null) {
            return new ModelAndView("redirect:/figures/" + figure.getId());
        }
        var posToDelete = new ArrayList<Position>(5);

        dx = dx == null ? 0 : dx;
        dy = dy == null ? 0 : dy;

        if (Math.abs(dx) + Math.abs(dy) != 1) {
            return new ModelAndView("redirect:/figures/" + figure.getId());
        }
        var pos = figure.getPosition();
        pos.setX(pos.getX() + dx);
        pos.setY(pos.getY() + dy);
        positionRepo.save(pos);
        figure.setStatMoves(figure.getStatMoves() + 1);

        // load near figures and carrots
        var near = positionRepo.findAllByXGreaterThanAndXLessThanAndYGreaterThanAndYLessThan(
                pos.getX() - 2, pos.getX() + 2, pos.getY() - 2, pos.getY() + 2);

        if (figure instanceof Rabbit rabbit) {
            // eat carrots
            if (!near.isEmpty()) {
                var carrots = carrotRepo.findByPositionIn(near);
                for (var carrot : carrots) {
                    var p = carrot.getPosition();
                    if (p.isAdjacent(pos)) {
                        rabbit.setNutrition(rabbit.getNutrition() + Rabbit.CARROT_NUTRITION);
                        rabbit.increaseScore(1);

                        posToDelete.add(p);
                        carrotRepo.delete(carrot);
                    }
                }
            }

            // die of hunger
            rabbit.setNutrition(rabbit.getNutrition() - 1);
            if (rabbit.getNutrition() <= 0) {
                posToDelete.add(rabbit.getPosition());
                rabbit.setPosition(null);
            }

            // get eaten
            if (!near.isEmpty()) {
                var foxes = foxRepo.findByPositionIn(near);
                for (var fox : foxes) {
                    var p = fox.getPosition();
                    if (p.isAdjacent(pos)) {
                        posToDelete.add(pos);
                        rabbit.setPosition(null);

                        fox.increaseScore(1 + rabbit.getScore() / 2);
                        figureRepo.save(fox);
                    }
                }
            }
        } else if (figure instanceof Fox fox) {
            // eat rabbits
            if (!near.isEmpty()) {
                var rabbits = rabbitRepo.findByPositionIn(near);
                for (var rabbit : rabbits) {
                    var p = rabbit.getPosition();
                    if (p.isAdjacent(pos)) {
                        posToDelete.add(p);
                        rabbit.setPosition(null);
                        figureRepo.save(rabbit);

                        fox.increaseScore(1 + rabbit.getScore() / 2);
                    }
                }
            }
        }

        figureRepo.save(figure);
        for (var p : posToDelete) {
            positionRepo.delete(p);
        }

        spawnService.tick();

        return new ModelAndView("redirect:/figures/" + figure.getId());
    }

    static final int GRID_SIZE = 21;
    static final int OFFSET = (int) (GRID_SIZE / 2);

    private List<Cell> renderCells(CellPos center, List<Figure> figures, List<Carrot> carrots) {
        // init cells
        var cells = new ArrayList<Cell>(GRID_SIZE * GRID_SIZE);
        for (var i = 0; i < GRID_SIZE * GRID_SIZE; i++) {
            var cell = new Cell();
            cell.x = i % GRID_SIZE + (center.x - OFFSET);
            cell.y = i / GRID_SIZE + (center.y - OFFSET);
            cell.href = "/figures/new?x=" + cell.x + "&y=" + cell.y;
            cells.add(cell);
        }

        for (var figure : figures) {
            if (figure.getPosition() == null) {
                continue;
            }
            if (figure.getPosition().getX() != center.x
                    || figure.getPosition().getY() != center.y) {
                continue;
            }

            var up = new CellPos(OFFSET, OFFSET - 1).getIndex();
            cells.get(up).href = "/figures/" + figure.getId() + "/move?dy=-1";
            cells.get(up).icon = "arrow-up.png";

            var down = new CellPos(OFFSET, OFFSET + 1).getIndex();
            cells.get(down).href = "/figures/" + figure.getId() + "/move?dy=1";
            cells.get(down).icon = "arrow-down.png";

            var left = new CellPos(OFFSET - 1, OFFSET).getIndex();
            cells.get(left).href = "/figures/" + figure.getId() + "/move?dx=-1";
            cells.get(left).icon = "arrow-left.png";

            var right = new CellPos(OFFSET + 1, OFFSET).getIndex();
            cells.get(right).href = "/figures/" + figure.getId() + "/move?dx=1";
            cells.get(right).icon = "arrow-right.png";

        }

        for (var figure : carrots) {
            var pos = figure.getPosition();
            if (pos == null) {
                continue;
            }
            var rel = this.calcRelativePosition(center, new CellPos(pos));
            if (rel == null) {
                continue;
            }

            var cellIndex = rel.getIndex();
            cells.get(cellIndex).icon = "carrot.png";
        }
        for (var figure : figures) {
            var pos = figure.getPosition();
            if (pos == null) {
                continue;
            }
            var rel = this.calcRelativePosition(center, new CellPos(pos));
            if (rel == null) {
                continue;
            }

            var cellIndex = rel.getIndex();
            cells.get(cellIndex).href = "/figures/" + figure.getId();
            cells.get(cellIndex).icon = this.getCellIcon(figure);

            if (figure instanceof Rabbit rabbit) {
                cells.get(cellIndex).decay =
                        Math.max(0, 100 * (Rabbit.CARROT_NUTRITION - rabbit.getNutrition())
                                / Rabbit.CARROT_NUTRITION);
            }
        }
        return cells;
    }

    private String getCellIcon(Figure figure) {
        if (figure instanceof Rabbit) {
            return "rabbit.png";
        } else if (figure instanceof Fox) {
            return "fox.png";
        }
        return null;
    }

    private CellPos calcRelativePosition(CellPos center, CellPos absolute) {
        var relX = absolute.x - (center.x - OFFSET);
        var relY = absolute.y - (center.y - OFFSET);

        if (relX < 0 || relX >= GRID_SIZE) {
            return null;
        }
        if (relY < 0 || relY >= GRID_SIZE) {
            return null;
        }
        return new CellPos(relX, relY);
    }

    class CellPos {
        int x;
        int y;

        CellPos(Position p) {
            this.x = p.getX();
            this.y = p.getY();
        }

        CellPos(int x, int y) {
            this.x = x;
            this.y = y;
        }

        int getIndex() {
            return this.x + this.y * GRID_SIZE;
        }
    }

    @GetMapping("/figures/new")
    public ModelAndView newFigureView(@RequestParam(name = "x", required = true) int x,
            @RequestParam(name = "y", required = true) int y, Model model) {
        // prevent creating figures at the same position
        var position = positionRepo.findByXAndY(x, y);
        if (position != null) {
            return new ModelAndView("redirect:/?x=" + x + "&y=" + y);
        }
        model.addAttribute("x", x);
        model.addAttribute("y", y);

        var rabbitNameSuggestion = spawnService.randomName(SpawnService.RABBIT_NAMES);
        model.addAttribute("nameSuggestion", rabbitNameSuggestion);

        return new ModelAndView("figure-new");
    }

    @PostMapping("/figures/new")
    @Transactional
    public ModelAndView newFigure(@RequestParam(name = "name", required = true) String name,
            @RequestParam(name = "x", required = true) int x,
            @RequestParam(name = "y", required = true) int y, Model model) {
        // prevent creating figures at the same position
        var position = positionRepo.findByXAndY(x, y);
        if (position != null) {
            return new ModelAndView("redirect:/?x=" + x + "&y=" + y);
        }

        var pos = new Position();
        pos.setX(x);
        pos.setY(y);
        positionRepo.save(pos);

        var figure = new Rabbit();
        figure.setName(name);
        figure.setPosition(pos);
        figure.setNutrition(Rabbit.CARROT_NUTRITION);
        figureRepo.save(figure);

        return new ModelAndView("redirect:/?x=" + x + "&y=" + y);
    }
}
