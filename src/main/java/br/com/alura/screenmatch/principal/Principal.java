package br.com.alura.screenmatch.principal;

import br.com.alura.screenmatch.models.DadosEpisodio;
import br.com.alura.screenmatch.models.DadosSerie;
import br.com.alura.screenmatch.models.DadosTemporada;
import br.com.alura.screenmatch.models.Episodio;
import br.com.alura.screenmatch.service.ConsumoApi;
import br.com.alura.screenmatch.service.ConverteDados;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

public class Principal {

    private Scanner leitura = new Scanner(System.in);
    private ConsumoApi consumo = new ConsumoApi();
    private ConverteDados conversor = new ConverteDados();

    private final String ENDERECO = "https://www.omdbapi.com/?t=";
    private final String APIKEY = "&apikey=63a6bc50";

    public void exibeMenu(){

        System.out.println("Digite o nome da série para busca: ");
        var nomeSerie = leitura.nextLine();
        var json = consumo.obterDados(ENDERECO + nomeSerie.replace(" ", "+") + APIKEY);

        DadosSerie dados = conversor.obterDados(json, DadosSerie.class);
        System.out.println(dados);

        List<DadosTemporada> temporadas = new ArrayList<>();

        System.out.println("----- BUSCANDO TODAS AS TEMPORADAS -----");
		for (int i = 1; i<=dados.totalTemporadas(); i++){
			json = consumo.obterDados(ENDERECO + nomeSerie.replace(" ", "+") + "&season=" + i + APIKEY);
			DadosTemporada dadosTemporada = conversor.obterDados(json, DadosTemporada.class);
			temporadas.add(dadosTemporada);
		};
		temporadas.forEach(System.out::println);

//        for (int i = 0; i < dados.totalTemporadas(); i++){
//            List<DadosEpisodio> episodiosTemporada = temporadas.get(i).episodios();
//            for (int j = 0; j < episodiosTemporada.size(); j++){
//                System.out.println(episodiosTemporada.get(j).titulo());
//            }
//        }
        // transformando aquele for gigantesco em um forEach usando lambda

        System.out.println("----- BUSCANDO TODSS OS EPISODIOS DAS TEMPORADAS -----");
        temporadas.forEach(t -> t.episodios().forEach(e -> System.out.println(e.titulo()))); // percorre todas as temporadas e depois percorre cada episodios das temporadas e imprimi

//        List<String> nomes = Arrays.asList("Jacque", "Iasmin", "Paulo", "Rodrigo", "Nico");
//
//        nomes.stream()
//                .sorted()
//                .limit(3)
//                .filter(n -> n.startsWith("N"))
//                .map(n -> n.toUpperCase())
//                .forEach(System.out::println);

        System.out.println("----- BUSCANDO OS 5 MELHORES EPISODIOS -----");
        List<DadosEpisodio> dadosEpisodios = temporadas.stream()
                .flatMap(t -> t.episodios().stream())
                .collect(Collectors.toList());

//        System.out.println("\n Top 10 episódios");
//        dadosEpisodios.stream()
//                .filter(e -> !e.avaliacao().equalsIgnoreCase("N/A")) // ignora se a avaliação for N/A
//                .peek(e -> System.out.println("Primeiro filtro(N/A) " + e)) // verifica o que acontece no segundo filtro
//                .sorted(Comparator.comparing(DadosEpisodio::avaliacao).reversed()) // faz uma comparação com os episodios e os ordena em ordem decrescente
//                .peek(e -> System.out.println("Ordenação " + e)) // verifica o que acontece na ordenação
//                .limit(10) // limita o total armazenado em 10
//                .peek(e -> System.out.println("Limite " + e)) // verifica
//                .map(e -> e.titulo().toUpperCase()) // percorre todos os titulos e os coloca em letra maiscula
//                .peek(e -> System.out.println("Mapeamento " + e)) // verifica o mapeamento
//                .forEach(System.out::println); // imprimi todos os episodios armazenados

        System.out.println("----- BUSCANDO OS DADOS DO EPISIDIOS JUNTO COM SUA RESPECTIVA TEMPORADA -----");
        List<Episodio> episodios = temporadas.stream() // cria um stream das temporadas
                .flatMap(t -> t.episodios().stream() // faz um stream de cada episodios das temporadas
                        .map(d -> new Episodio(t.numero(), d)) // percorre todos os dados dos EPS e os transforma em novos episodios para depois armazenar em um collector
                ).collect(Collectors.toList());

        episodios.forEach(System.out::println);

        System.out.println("Digite um trecho do titulo do EP.");
        var trechoTitulo = leitura.nextLine();
        Optional<Episodio> episodioBuscado = episodios.stream() // o Optional cria um container que vai armazenar um valor se o valor for encontrado
                .filter(e -> e.getTitulo().toUpperCase().contains(trechoTitulo.toUpperCase())) // pegando os titulos em maisculo e verificando se tem algum titulo com o mesmo valor que a variavel
                .findFirst(); // encontra o primeiro trecho encontrado
        if (episodioBuscado.isPresent()){ // isPresent serve para ver se tem algum valor dentro da variavel
            System.out.println("EP encontrado");
            System.out.println("Temporada: " + episodioBuscado.get().getTemporada()); // imprimindo a temporada do episodio encontrado
        } else {
            System.out.println("EP não encontrado");
        }


//        System.out.println("A partir de que ano você deseja ver os episodios? ");
//        var ano = leitura.nextInt();
//        leitura.nextLine();
//
//        LocalDate dataBusca = LocalDate.of(ano, 1, 1); // vai buscar a partir do ano informado no dia 1 do mes 1
//
//        DateTimeFormatter formatador = DateTimeFormatter.ofPattern("dd/MM/yyyy"); // formato um novo jeito de armazenas datas
//        episodios.stream()
//                .filter(e -> e.getDataLancamento() != null && e.getDataLancamento().isAfter(dataBusca)) // filtra por episoios que tem uma data e que o lancamento seja depois da data de busca
//                .forEach(e -> System.out.println(
//                        "Temporada: " + e.getTemporada() +
//                                ", Episodio: " + e.getTitulo() +
//                                ", Data lançamento: " + e.getDataLancamento().format(formatador)
//                ));

        Map<Integer, Double> avaliacoesPorTemporada = episodios.stream()
                .filter(e -> !e.getAvaliacao().equals(0.0))
                .collect(Collectors.groupingBy(Episodio::getTemporada,
                        Collectors.averagingDouble(Episodio::getAvaliacao)));
        System.out.println(avaliacoesPorTemporada);

        DoubleSummaryStatistics est = episodios.stream()
                .filter(e -> e.getAvaliacao() > 0.0)
                .collect(Collectors.summarizingDouble(Episodio::getAvaliacao));
        System.out.println("Quatidade de EPS: " + est.getCount());
        System.out.println("Media: " + est.getAverage());
        System.out.println("Melhor EP: " + est.getMax());
        System.out.println("Pior EP: " + est.getMin());


   }
}
