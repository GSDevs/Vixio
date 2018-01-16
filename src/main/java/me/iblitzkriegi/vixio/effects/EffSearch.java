package me.iblitzkriegi.vixio.effects;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.Variable;
import ch.njol.skript.lang.VariableString;
import ch.njol.util.Kleenean;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import me.iblitzkriegi.vixio.Vixio;
import me.iblitzkriegi.vixio.util.AsyncEffect;
import me.iblitzkriegi.vixio.util.Util;
import me.iblitzkriegi.vixio.util.enums.SearchSite;
import org.bukkit.event.Event;

import java.util.Locale;

public class EffSearch extends AsyncEffect {

    public static AudioTrack[] lastResults;

    static {
        Vixio.getInstance().registerEffect(EffSearch.class, "search <youtube|soundcloud> for %strings% [and store the results in %-objects%]");
    }

    private SearchSite site;
    private Expression<String> queries;
    private boolean local;
    private VariableString variable;

    @Override
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
        queries = (Expression<String>) exprs[0];
        if (exprs[1] instanceof Variable) {
            Variable<?> varExpr = (Variable<?>) exprs[1];
            variable = Util.getVariableName(varExpr);
            if (!varExpr.isList()) {
                Skript.error(variable + " is not a list variable");
                return false;
            }
            local = varExpr.isLocal();
        }

        try {
            site = SearchSite.valueOf(parseResult.regexes.get(0).group(0).toUpperCase(Locale.ENGLISH));
        } catch (IllegalArgumentException e) {
        }

        return true;
    }

    @Override
    public void execute(Event e) {
        AudioTrack[] results = Util.search(site, queries.getAll(e));
        lastResults = results;
        if (variable != null) {
            String variable = this.variable.toString(e);
            int separatorLength = Variable.SEPARATOR.length() + 1;
            variable = variable.substring(0, (variable.length() - separatorLength));
            Util.setList(variable, results, e, local);
        }

    }

    @Override
    public String toString(Event event, boolean debug) {
        return "search " + site.name().toLowerCase(Locale.ENGLISH) + " for " + queries.toString(event, debug) + (variable == null ? "" : " and store the results in " + variable.toString(event, debug));
    }

}
