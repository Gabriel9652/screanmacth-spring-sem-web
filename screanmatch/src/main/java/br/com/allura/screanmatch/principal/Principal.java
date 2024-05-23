package br.com.allura.screanmatch.principal;

import br.com.allura.screanmatch.model.DadosEpisodeo;
import br.com.allura.screanmatch.model.DadosSerie;
import br.com.allura.screanmatch.model.DadosTemporada;
import br.com.allura.screanmatch.model.Episodeo;
import br.com.allura.screanmatch.service.ConsumoApi;
import br.com.allura.screanmatch.service.ConverteDados;


import java.util.*;
import java.util.stream.Collectors;


public class Principal {
    private final String ENDERECO = "https://www.omdbapi.com/?t=";
    private final String APIKEY = "&apikey=f393087a";
    private Scanner leitura = new Scanner(System.in);
    private ConsumoApi consumoApi = new ConsumoApi();
    private ConverteDados conversor = new ConverteDados();


    public void exibeMenu(){
        System.out.println("Digite o nome da serie: ");
        var nomeSerie = leitura.nextLine();
        var json = consumoApi.obterDados(ENDERECO + nomeSerie.replace(" ","+") + APIKEY);
        DadosSerie dados = conversor.obterDados(json, DadosSerie.class);
        System.out.println(dados);

        List<DadosTemporada> temporadas = new ArrayList<>();

        for(int i = 1; i<= dados.totalTemporadas(); i++){
            json = consumoApi.obterDados(ENDERECO + nomeSerie.replace(" ","+") + "&season=" + i + APIKEY);
			DadosTemporada dadosTemporada = conversor.obterDados(json, DadosTemporada.class);
			temporadas.add(dadosTemporada);
		}
		temporadas.forEach(System.out::println);

        temporadas.forEach(t-> t.episodeos().forEach(e-> System.out.println(e.titulo())));

        List<DadosEpisodeo> dadosEpisodeos = temporadas.stream()
                .flatMap(t -> t.episodeos().stream())
                .collect(Collectors.toList());


        System.out.println("\n top 5 episodeos");
        dadosEpisodeos.stream()
                .filter(e -> !e.avaliacao().equalsIgnoreCase("N/A"))
                .sorted(Comparator.comparing(DadosEpisodeo::avaliacao).reversed())
                .limit(5)
                .forEach(System.out::println);

        List<Episodeo> episodeos = temporadas.stream()
                .flatMap(t -> t.episodeos().stream()
                .map(d -> new Episodeo(t.numero(),d)))
                .collect(Collectors.toList());

        episodeos.forEach(System.out::println);

//        System.out.println("Digite o trecho do titulo");
//        var trechoTitulo = leitura.nextLine();
//
//        Optional<Episodeo> episodeoBuscado = episodeos.stream()
//                .filter(e -> e.getTitulo().toUpperCase().contains(trechoTitulo.toUpperCase()))
//                .findFirst();
//
//        if(episodeoBuscado.isPresent()){
//            System.out.println("Episódeo encontrado!");
//            System.out.println("Temporada: " + episodeoBuscado.get());
//        }else {
//            System.out.println("Episódeo não encontrado");
//        }

//        System.out.println("A partir de que ano você quer ver os episódeos?");
//        var ano = leitura.nextInt();
//        leitura.nextLine();
//
//        LocalDate dataBusca = LocalDate.of(ano,1,1);
//
//        DateTimeFormatter formatador = DateTimeFormatter.ofPattern("dd/MM/yyyy");
//
//        episodeos.stream()
//                .filter(e -> e.getDataLancamento() != null && e.getDataLancamento().isAfter(dataBusca)
//                        ).forEach(episodeo -> System.out.println(
//                                "Temporada: " + episodeo.getTemporada() +
//                                        ", Episódeo: " + episodeo.getTitulo() +
//                                        ", Data de lançamento: " + episodeo.getDataLancamento().format(formatador)
//                ));


        Map<Integer,Double> avaliacoesPorTemporada = episodeos.stream()
                .filter(e -> e.getAvaliacao()>0.0)
                .collect(Collectors.groupingBy(Episodeo::getTemporada,
                        Collectors.averagingDouble(Episodeo::getAvaliacao)));

        System.out.println(avaliacoesPorTemporada);

        DoubleSummaryStatistics est = episodeos.stream()
                .filter(e -> e.getAvaliacao()>0.0)
                .collect(Collectors.summarizingDouble(Episodeo::getAvaliacao));
        System.out.println("Media: "+est.getAverage());
        System.out.println("Avaliação minima: "+est.getMin());
        System.out.println("Avaliação maxima: "+est.getMax());

    }

}
