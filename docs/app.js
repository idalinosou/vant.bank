/* -------------------------------------------------------------------------
   vant.bank — interface dual-mode
   API Spring Boot quando servida pela aplicação; localStorage no GitHub Pages.
   ------------------------------------------------------------------------- */

const CHAVE_DEMO = "vant.bank.demo.v1";
const TAMANHO_PAGINA = 8;
const TAMANHO_EXTRATO = 10;

const STATUS_CONTA = {
  ATIVA: "Ativa",
  BLOQUEADA: "Bloqueada",
  ENCERRADA: "Encerrada"
};

const TIPO_CONTA = {
  CORRENTE: "Corrente",
  POUPANCA: "Poupança"
};

const TIPOS_MOV = {
  DEPOSITO: { rotulo: "Depósito", entrada: true },
  SAQUE: { rotulo: "Saque", entrada: false },
  TRANSFERENCIA_ENVIADA: { rotulo: "Transferência enviada", entrada: false },
  TRANSFERENCIA_RECEBIDA: { rotulo: "Transferência recebida", entrada: true }
};

const el = {
  seloModo: document.querySelector("#selo-modo"),
  atualizarTudo: document.querySelector("#atualizar-tudo"),
  avisoGeral: document.querySelector("#aviso-geral"),

  cartaoSaldo: document.querySelector("#cartao-saldo"),
  cartaoAtivas: document.querySelector("#cartao-ativas"),
  cartaoBloqueadas: document.querySelector("#cartao-bloqueadas"),
  cartaoVolume: document.querySelector("#cartao-volume"),
  cartaoTransferencias: document.querySelector("#cartao-transferencias"),
  corpoUltimas: document.querySelector("#corpo-ultimas"),
  recursoTotal: document.querySelector("#recurso-total"),
  recursoAtivas: document.querySelector("#recurso-ativas"),
  recursoBloqueadas: document.querySelector("#recurso-bloqueadas"),
  recursoEncerradas: document.querySelector("#recurso-encerradas"),
  alertaBloqueadas: document.querySelector("#alerta-bloqueadas"),
  textoBloqueadas: document.querySelector("#texto-bloqueadas"),

  chipsStatus: document.querySelector("#chips-status"),
  buscaContas: document.querySelector("#busca-contas"),
  botaoNovaConta: document.querySelector("#botao-nova-conta"),
  painelFormConta: document.querySelector("#painel-form-conta"),
  tituloFormConta: document.querySelector("#titulo-form-conta"),
  fecharFormConta: document.querySelector("#fechar-form-conta"),
  formConta: document.querySelector("#form-conta"),
  contaNumero: document.querySelector("#conta-numero"),
  contaTitular: document.querySelector("#conta-titular"),
  contaDocumento: document.querySelector("#conta-documento"),
  contaTipo: document.querySelector("#conta-tipo"),
  contaLimite: document.querySelector("#conta-limite"),
  salvarConta: document.querySelector("#salvar-conta"),
  cancelarConta: document.querySelector("#cancelar-conta"),

  painelMovimento: document.querySelector("#painel-movimento"),
  tituloMovimento: document.querySelector("#titulo-movimento"),
  fecharMovimento: document.querySelector("#fechar-movimento"),
  formMovimento: document.querySelector("#form-movimento"),
  movimentoTipo: document.querySelector("#movimento-tipo"),
  movimentoValor: document.querySelector("#movimento-valor"),
  movimentoDescricao: document.querySelector("#movimento-descricao"),
  salvarMovimento: document.querySelector("#salvar-movimento"),
  cancelarMovimento: document.querySelector("#cancelar-movimento"),

  avisoContas: document.querySelector("#aviso-contas"),
  contagemContas: document.querySelector("#contagem-contas"),
  corpoContas: document.querySelector("#corpo-contas"),
  paginacaoContas: document.querySelector("#paginacao-contas"),

  formTransferencia: document.querySelector("#form-transferencia"),
  transferenciaOrigem: document.querySelector("#transferencia-origem"),
  transferenciaDestino: document.querySelector("#transferencia-destino"),
  transferenciaValor: document.querySelector("#transferencia-valor"),
  transferenciaDescricao: document.querySelector("#transferencia-descricao"),
  salvarTransferencia: document.querySelector("#salvar-transferencia"),
  dicaTransferencia: document.querySelector("#dica-transferencia"),
  reciboTransferencia: document.querySelector("#recibo-transferencia"),
  reciboDetalhe: document.querySelector("#recibo-detalhe"),
  reciboSaldoOrigem: document.querySelector("#recibo-saldo-origem"),
  reciboSaldoDestino: document.querySelector("#recibo-saldo-destino"),
  corpoTransferencias: document.querySelector("#corpo-transferencias"),

  extratoConta: document.querySelector("#extrato-conta"),
  extratoTitulo: document.querySelector("#extrato-titulo"),
  extratoSubtitulo: document.querySelector("#extrato-subtitulo"),
  resumoExtrato: document.querySelector("#resumo-extrato"),
  extratoSaldo: document.querySelector("#extrato-saldo"),
  extratoLimite: document.querySelector("#extrato-limite"),
  extratoStatus: document.querySelector("#extrato-status"),
  chipsExtrato: document.querySelector("#chips-extrato"),
  corpoExtrato: document.querySelector("#corpo-extrato"),
  paginacaoExtrato: document.querySelector("#paginacao-extrato"),

  toast: document.querySelector("#toast"),
  toastTexto: document.querySelector("#toast-texto")
};

const moeda = new Intl.NumberFormat("pt-BR", { style: "currency", currency: "BRL" });
const numero = new Intl.NumberFormat("pt-BR", { minimumFractionDigits: 2, maximumFractionDigits: 2 });

const estado = {
  modo: null,
  vista: "painel",
  contas: { busca: "", status: "", pagina: 1, cache: null },
  todasContas: [],
  editandoContaId: null,
  movendoContaId: null,
  extrato: { contaId: null, tipo: "", pagina: 1 },
  confirmacoes: {},
  temporizadores: new Map()
};

/* -------------------------------------------------------------------------
   Formatação e utilidades
   ------------------------------------------------------------------------- */

function arredondar(valor) {
  return Math.round((Number(valor) + Number.EPSILON) * 100) / 100;
}

function formatarMoeda(valor) {
  return moeda.format(Number(valor) || 0);
}

function formatarNumero(valor) {
  return numero.format(Number(valor) || 0);
}

function formatarData(iso) {
  if (!iso) return "—";
  return new Date(iso).toLocaleString("pt-BR", {
    day: "2-digit",
    month: "2-digit",
    year: "numeric",
    hour: "2-digit",
    minute: "2-digit"
  });
}

function valorDe(texto) {
  let limpo = String(texto ?? "").trim().replace(/R\$/g, "").replace(/\s/g, "");
  if (!limpo) return NaN;
  if (limpo.includes(",")) {
    limpo = limpo.replace(/\./g, "").replace(",", ".");
  }
  const valor = Number(limpo);
  return Number.isFinite(valor) ? arredondar(valor) : NaN;
}

function esc(valor) {
  return String(valor ?? "")
    .replace(/&/g, "&amp;")
    .replace(/</g, "&lt;")
    .replace(/>/g, "&gt;")
    .replace(/"/g, "&quot;")
    .replace(/'/g, "&#39;");
}

function dataDias(diasAtras) {
  const data = new Date();
  data.setDate(data.getDate() - diasAtras);
  data.setHours(10, 0, 0, 0);
  return data.toISOString();
}

function assinaturaValor(movimentacao) {
  const sinal = movimentacao.entrada ? "+" : "−";
  return `${sinal} ${formatarMoeda(movimentacao.valor)}`;
}

function classeValor(entrada) {
  return entrada ? "valor-positivo" : "valor-negativo";
}

function classeStatus(status) {
  return "status-" + String(status).toLowerCase();
}

/* -------------------------------------------------------------------------
   Modo demonstração (localStorage)
   ------------------------------------------------------------------------- */

const demo = (() => {
  let banco = carregar();

  function carregar() {
    try {
      const salvo = localStorage.getItem(CHAVE_DEMO);
      if (salvo) {
        const dados = JSON.parse(salvo);
        if (dados && Array.isArray(dados.contas) && Array.isArray(dados.movimentacoes)) {
          return dados;
        }
      }
    } catch (erro) {
      console.warn("Falha ao ler os dados locais.", erro);
    }
    return semear();
  }

  function persistir() {
    localStorage.setItem(CHAVE_DEMO, JSON.stringify(banco));
  }

  function semear() {
    const novo = { sequencia: { conta: 0, movimentacao: 0 }, contas: [], movimentacoes: [] };

    const criar = (numeroConta, titular, documento, tipo, limite, status, diasCriada) => {
      const conta = {
        id: ++novo.sequencia.conta,
        numero: numeroConta,
        titular,
        documento,
        tipo,
        status,
        saldo: 0,
        limite: arredondar(limite),
        criadaEm: dataDias(diasCriada)
      };
      novo.contas.push(conta);
      return conta;
    };

    const registrar = (conta, tipo, valor, diasAtras, descricao, contraparte) => {
      const quantia = arredondar(valor);
      if (TIPOS_MOV[tipo].entrada) {
        conta.saldo = arredondar(conta.saldo + quantia);
      } else {
        conta.saldo = arredondar(conta.saldo - quantia);
      }
      novo.movimentacoes.push({
        id: ++novo.sequencia.movimentacao,
        contaId: conta.id,
        tipo,
        valor: quantia,
        saldoPosterior: conta.saldo,
        descricao,
        contraparte,
        criadaEm: dataDias(diasAtras)
      });
    };

    const transferir = (origem, destino, valor, diasAtras) => {
      const quantia = arredondar(valor);
      origem.saldo = arredondar(origem.saldo - quantia);
      destino.saldo = arredondar(destino.saldo + quantia);
      const quando = dataDias(diasAtras);
      novo.movimentacoes.push({
        id: ++novo.sequencia.movimentacao,
        contaId: origem.id,
        tipo: "TRANSFERENCIA_ENVIADA",
        valor: quantia,
        saldoPosterior: origem.saldo,
        descricao: "Transferência entre contas",
        contraparte: destino.numero + " · " + destino.titular,
        criadaEm: quando
      });
      novo.movimentacoes.push({
        id: ++novo.sequencia.movimentacao,
        contaId: destino.id,
        tipo: "TRANSFERENCIA_RECEBIDA",
        valor: quantia,
        saldoPosterior: destino.saldo,
        descricao: "Transferência entre contas",
        contraparte: origem.numero + " · " + origem.titular,
        criadaEm: quando
      });
    };

    const ana = criar("1001-1", "Ana Souza", "123.456.789-00", "CORRENTE", 2000, "ATIVA", 40);
    const bruno = criar("1002-2", "Bruno Lima", "987.654.321-00", "CORRENTE", 1000, "ATIVA", 38);
    const carla = criar("1003-3", "Carla Mendes", "456.789.123-00", "POUPANCA", 0, "ATIVA", 35);
    const diego = criar("1004-4", "Diego Rocha", "321.654.987-00", "CORRENTE", 500, "BLOQUEADA", 33);
    const fernanda = criar("1005-5", "Fernanda Alves", "654.321.987-00", "CORRENTE", 5000, "ATIVA", 32);
    criar("1006-6", "Gustavo Reis", "789.123.456-00", "POUPANCA", 0, "ENCERRADA", 30);

    registrar(ana, "DEPOSITO", 12000, 9, "Depósito inicial", null);
    registrar(ana, "SAQUE", 1550, 7, "Saque em dinheiro", null);
    transferir(ana, bruno, 2000, 3);

    registrar(bruno, "DEPOSITO", 3280.5, 8, "Depósito em dinheiro", null);
    registrar(bruno, "SAQUE", 1000, 2, "Saque em dinheiro", null);
    transferir(bruno, carla, 1000, 1);

    registrar(carla, "DEPOSITO", 10000, 6, "Depósito inicial", null);
    registrar(carla, "DEPOSITO", 1900, 1, "Depósito em dinheiro", null);

    registrar(diego, "DEPOSITO", 950, 5, "Depósito em dinheiro", null);

    registrar(fernanda, "DEPOSITO", 15000, 4, "Depósito inicial", null);
    registrar(fernanda, "DEPOSITO", 200, 2, "Depósito em dinheiro", null);

    localStorage.setItem(CHAVE_DEMO, JSON.stringify(novo));
    return novo;
  }

  function contaDe(id) {
    const conta = banco.contas.find((item) => item.id === Number(id));
    if (!conta) throw new Error("Conta não encontrada.");
    return conta;
  }

  function respostaConta(conta) {
    return {
      id: conta.id,
      numero: conta.numero,
      titular: conta.titular,
      documento: conta.documento,
      tipo: conta.tipo,
      tipoRotulo: TIPO_CONTA[conta.tipo],
      status: conta.status,
      statusRotulo: STATUS_CONTA[conta.status],
      saldo: conta.saldo,
      limite: conta.limite,
      criadaEm: conta.criadaEm
    };
  }

  function respostaMov(movimentacao) {
    const conta = contaDe(movimentacao.contaId);
    return {
      id: movimentacao.id,
      contaId: conta.id,
      contaNumero: conta.numero,
      contaTitular: conta.titular,
      tipo: movimentacao.tipo,
      tipoRotulo: TIPOS_MOV[movimentacao.tipo].rotulo,
      entrada: TIPOS_MOV[movimentacao.tipo].entrada,
      valor: movimentacao.valor,
      saldoPosterior: movimentacao.saldoPosterior,
      descricao: movimentacao.descricao,
      contraparte: movimentacao.contraparte,
      criadaEm: movimentacao.criadaEm
    };
  }

  function registrarMov(conta, tipo, valor, descricao, contraparte, quando) {
    const movimentacao = {
      id: ++banco.sequencia.movimentacao,
      contaId: conta.id,
      tipo,
      valor: arredondar(valor),
      saldoPosterior: conta.saldo,
      descricao,
      contraparte,
      criadaEm: quando || new Date().toISOString()
    };
    banco.movimentacoes.push(movimentacao);
    return movimentacao;
  }

  function pagina(itens, paginaAtual, tamanho) {
    const total = itens.length;
    const totalPaginas = Math.max(1, Math.ceil(total / tamanho));
    const atual = Math.min(Math.max(1, paginaAtual), totalPaginas);
    return {
      content: itens.slice((atual - 1) * tamanho, atual * tamanho),
      totalElements: total,
      totalPages: totalPaginas,
      number: atual - 1,
      size: tamanho
    };
  }

  function listarContas(filtro = {}) {
    const busca = (filtro.busca || "").trim().toLowerCase();
    let lista = banco.contas.slice();
    if (busca) {
      lista = lista.filter((conta) =>
        [conta.numero, conta.titular, conta.documento || ""].some((valor) =>
          String(valor).toLowerCase().includes(busca)));
    }
    if (filtro.status) {
      lista = lista.filter((conta) => conta.status === filtro.status);
    }
    lista.sort((a, b) => a.numero.localeCompare(b.numero, "pt-BR"));
    const resultado = pagina(lista, Number(filtro.pagina) || 1, Number(filtro.tamanho) || TAMANHO_PAGINA);
    return { ...resultado, content: resultado.content.map(respostaConta) };
  }

  function criarConta(pedido) {
    const numeroConta = String(pedido.numero || "").trim();
    if (banco.contas.some((conta) => conta.numero.toLowerCase() === numeroConta.toLowerCase())) {
      throw new Error(`Já existe uma conta com o número "${numeroConta}".`);
    }
    const conta = {
      id: ++banco.sequencia.conta,
      numero: numeroConta,
      titular: String(pedido.titular || "").trim(),
      documento: pedido.documento ? String(pedido.documento).trim() : null,
      tipo: pedido.tipo,
      status: "ATIVA",
      saldo: 0,
      limite: pedido.tipo === "POUPANCA" ? 0 : arredondar(pedido.limite || 0),
      criadaEm: new Date().toISOString()
    };
    banco.contas.push(conta);
    persistir();
    return respostaConta(conta);
  }

  function atualizarConta(id, pedido) {
    const conta = contaDe(id);
    if (conta.status === "ENCERRADA") {
      throw new Error("Conta encerrada não pode ser editada.");
    }
    conta.titular = String(pedido.titular || "").trim();
    conta.documento = pedido.documento ? String(pedido.documento).trim() : null;
    conta.tipo = pedido.tipo;
    conta.limite = pedido.tipo === "POUPANCA" ? 0 : arredondar(pedido.limite || 0);
    persistir();
    return respostaConta(conta);
  }

  function alterarStatus(id, novo) {
    const conta = contaDe(id);
    if (conta.status === novo) return respostaConta(conta);
    const permitidos = {
      ATIVA: ["BLOQUEADA", "ENCERRADA"],
      BLOQUEADA: ["ATIVA", "ENCERRADA"],
      ENCERRADA: []
    };
    if (!permitidos[conta.status].includes(novo)) {
      throw new Error(`Não é possível alterar o status da conta de "${STATUS_CONTA[conta.status]}" para "${STATUS_CONTA[novo]}".`);
    }
    if (novo === "ENCERRADA" && arredondar(conta.saldo) !== 0) {
      throw new Error("A conta só pode ser encerrada com o saldo zerado.");
    }
    conta.status = novo;
    persistir();
    return respostaConta(conta);
  }

  function exigirAtiva(conta) {
    if (conta.status !== "ATIVA") {
      throw new Error(`A conta "${conta.numero}" não está ativa para movimentações.`);
    }
  }

  function depositar(id, valor, descricao) {
    const conta = contaDe(id);
    exigirAtiva(conta);
    conta.saldo = arredondar(conta.saldo + valor);
    registrarMov(conta, "DEPOSITO", valor, descricao || "Depósito em dinheiro", null);
    persistir();
    return respostaConta(conta);
  }

  function sacar(id, valor, descricao) {
    const conta = contaDe(id);
    exigirAtiva(conta);
    if (arredondar(conta.saldo + conta.limite) < valor) {
      throw new Error(`Saldo e limite insuficientes na conta "${conta.numero}".`);
    }
    conta.saldo = arredondar(conta.saldo - valor);
    registrarMov(conta, "SAQUE", valor, descricao || "Saque em dinheiro", null);
    persistir();
    return respostaConta(conta);
  }

  function transferir(pedido) {
    const origemId = Number(pedido.origemId);
    const destinoId = Number(pedido.destinoId);
    if (origemId === destinoId) {
      throw new Error("A conta de origem deve ser diferente da conta de destino.");
    }
    const origem = contaDe(origemId);
    const destino = contaDe(destinoId);
    exigirAtiva(origem);
    if (destino.status !== "ATIVA") {
      throw new Error(`A conta de destino "${destino.numero}" não está ativa para receber transferências.`);
    }
    const valor = arredondar(pedido.valor);
    if (arredondar(origem.saldo + origem.limite) < valor) {
      throw new Error(`Saldo e limite insuficientes na conta "${origem.numero}".`);
    }
    const descricao = pedido.descricao || "Transferência entre contas";
    const quando = new Date().toISOString();
    origem.saldo = arredondar(origem.saldo - valor);
    destino.saldo = arredondar(destino.saldo + valor);
    registrarMov(origem, "TRANSFERENCIA_ENVIADA", valor, descricao,
      destino.numero + " · " + destino.titular, quando);
    registrarMov(destino, "TRANSFERENCIA_RECEBIDA", valor, descricao,
      origem.numero + " · " + origem.titular, quando);
    persistir();
    return {
      origemNumero: origem.numero,
      origemTitular: origem.titular,
      destinoNumero: destino.numero,
      destinoTitular: destino.titular,
      valor,
      saldoOrigem: origem.saldo,
      saldoDestino: destino.saldo,
      descricao,
      criadaEm: quando
    };
  }

  function extrato(contaId, tipo, paginaAtual, tamanho) {
    contaDe(contaId);
    let lista = banco.movimentacoes.filter((mov) => mov.contaId === Number(contaId));
    if (tipo) {
      lista = lista.filter((mov) => mov.tipo === tipo);
    }
    lista.sort((a, b) => new Date(b.criadaEm) - new Date(a.criadaEm) || b.id - a.id);
    const resultado = pagina(lista, paginaAtual, tamanho);
    return { ...resultado, content: resultado.content.map(respostaMov) };
  }

  function ultimas() {
    return banco.movimentacoes.slice()
      .sort((a, b) => new Date(b.criadaEm) - new Date(a.criadaEm) || b.id - a.id)
      .slice(0, 6)
      .map(respostaMov);
  }

  function recentes() {
    return banco.movimentacoes
      .filter((mov) => mov.tipo === "TRANSFERENCIA_ENVIADA")
      .sort((a, b) => new Date(b.criadaEm) - new Date(a.criadaEm) || b.id - a.id)
      .slice(0, 8)
      .map(respostaMov);
  }

  function painel() {
    const agora = new Date();
    const inicio = new Date(agora.getFullYear(), agora.getMonth(), 1);
    const fim = new Date(agora.getFullYear(), agora.getMonth() + 1, 1);
    const doMes = banco.movimentacoes.filter((mov) => {
      const data = new Date(mov.criadaEm);
      return data >= inicio && data < fim;
    });
    return {
      totalContas: banco.contas.length,
      ativas: banco.contas.filter((conta) => conta.status === "ATIVA").length,
      bloqueadas: banco.contas.filter((conta) => conta.status === "BLOQUEADA").length,
      encerradas: banco.contas.filter((conta) => conta.status === "ENCERRADA").length,
      saldoTotal: arredondar(banco.contas
        .filter((conta) => conta.status !== "ENCERRADA")
        .reduce((total, conta) => total + conta.saldo, 0)),
      volumeMes: arredondar(doMes.reduce((total, mov) => total + mov.valor, 0)),
      transferenciasMes: doMes.filter((mov) => mov.tipo === "TRANSFERENCIA_ENVIADA").length,
      ultimas: ultimas()
    };
  }

  return {
    listarContas,
    criarConta,
    atualizarConta,
    alterarStatus,
    depositar,
    sacar,
    transferir,
    extrato,
    recentes,
    painel
  };
})();

/* -------------------------------------------------------------------------
   Camada de dados (API ou demonstração)
   ------------------------------------------------------------------------- */

async function requisicao(caminho, opcoes = {}) {
  const resposta = await fetch(caminho, {
    headers: { Accept: "application/json", ...(opcoes.body ? { "Content-Type": "application/json" } : {}) },
    ...opcoes
  });
  if (!resposta.ok) {
    let mensagem = "Não foi possível concluir a operação.";
    try {
      const corpo = await resposta.json();
      if (corpo && corpo.mensagem) mensagem = corpo.mensagem;
    } catch (erro) {
      // resposta sem corpo JSON
    }
    throw new Error(mensagem);
  }
  if (resposta.status === 204) return null;
  return resposta.json();
}

function enviar(caminho, metodo, corpo) {
  return requisicao(caminho, { method: metodo, body: JSON.stringify(corpo) });
}

const dados = {
  listarContas(filtro = {}) {
    if (estado.modo === "demo") return Promise.resolve(demo.listarContas(filtro));
    const params = new URLSearchParams();
    if (filtro.busca) params.set("busca", filtro.busca);
    if (filtro.status) params.set("status", filtro.status);
    params.set("pagina", String(filtro.pagina || 1));
    params.set("tamanho", String(filtro.tamanho || TAMANHO_PAGINA));
    return requisicao("api/contas?" + params.toString());
  },
  criarConta(pedido) {
    return estado.modo === "demo" ? Promise.resolve(demo.criarConta(pedido)) : enviar("api/contas", "POST", pedido);
  },
  atualizarConta(id, pedido) {
    return estado.modo === "demo"
      ? Promise.resolve(demo.atualizarConta(id, pedido))
      : enviar(`api/contas/${id}`, "PUT", pedido);
  },
  alterarStatus(id, novoStatus) {
    return estado.modo === "demo"
      ? Promise.resolve(demo.alterarStatus(id, novoStatus))
      : enviar(`api/contas/${id}/status`, "PATCH", { status: novoStatus });
  },
  depositar(id, valor, descricao) {
    return estado.modo === "demo"
      ? Promise.resolve(demo.depositar(id, valor, descricao))
      : enviar(`api/contas/${id}/depositos`, "POST", { valor, descricao });
  },
  sacar(id, valor, descricao) {
    return estado.modo === "demo"
      ? Promise.resolve(demo.sacar(id, valor, descricao))
      : enviar(`api/contas/${id}/saques`, "POST", { valor, descricao });
  },
  transferir(pedido) {
    return estado.modo === "demo"
      ? Promise.resolve(demo.transferir(pedido))
      : enviar("api/transferencias", "POST", pedido);
  },
  extrato(contaId, tipo, paginaAtual, tamanho) {
    if (estado.modo === "demo") {
      return Promise.resolve(demo.extrato(contaId, tipo, paginaAtual, tamanho));
    }
    const params = new URLSearchParams();
    if (tipo) params.set("tipo", tipo);
    params.set("pagina", String(paginaAtual));
    params.set("tamanho", String(tamanho));
    return requisicao(`api/contas/${contaId}/extrato?` + params.toString());
  },
  recentes() {
    return estado.modo === "demo" ? Promise.resolve(demo.recentes()) : requisicao("api/transferencias/recentes");
  },
  painel() {
    return estado.modo === "demo" ? Promise.resolve(demo.painel()) : requisicao("api/painel");
  }
};

/* -------------------------------------------------------------------------
   Avisos e confirmação em duas etapas
   ------------------------------------------------------------------------- */

let tempoToast = null;

function mostrarToast(texto, tipo = "") {
  el.toastTexto.textContent = texto;
  el.toast.className = tipo ? "toast " + tipo : "toast";
  el.toast.hidden = false;
  clearTimeout(tempoToast);
  tempoToast = setTimeout(() => {
    el.toast.hidden = true;
  }, 3600);
}

function mostrarErro(falha) {
  mostrarToast(falha?.message || "Não foi possível concluir a operação.", "erro");
}

function duasEtapas(chave, id, renderizar, executar) {
  if (estado.confirmacoes[chave] === id) {
    estado.confirmacoes[chave] = null;
    clearTimeout(estado.temporizadores.get(chave));
    estado.temporizadores.delete(chave);
    executar();
    return;
  }
  estado.confirmacoes[chave] = id;
  renderizar();
  clearTimeout(estado.temporizadores.get(chave));
  estado.temporizadores.set(chave, setTimeout(() => {
    estado.confirmacoes[chave] = null;
    renderizar();
  }, 8000));
}

function renderizarChips(container, opcoes, valorAtivo, atributo) {
  container.innerHTML = opcoes.map((opcao) =>
    `<button type="button" class="chip ${opcao.valor === valorAtivo ? "ativo" : ""}" ${atributo}="${esc(opcao.valor)}">${esc(opcao.rotulo)}</button>`
  ).join("");
}

function renderizarPaginacao(container, paginaAtual, totalPaginas) {
  if (totalPaginas <= 1) {
    container.innerHTML = "";
    return;
  }
  let html = `<button type="button" data-pagina="${paginaAtual - 1}" ${paginaAtual <= 1 ? "disabled" : ""} aria-label="Página anterior">‹</button>`;
  for (let indice = 1; indice <= totalPaginas; indice++) {
    html += `<button type="button" data-pagina="${indice}" class="${indice === paginaAtual ? "ativo" : ""}">${indice}</button>`;
  }
  html += `<button type="button" data-pagina="${paginaAtual + 1}" ${paginaAtual >= totalPaginas ? "disabled" : ""} aria-label="Próxima página">›</button>`;
  container.innerHTML = html;
}

/* -------------------------------------------------------------------------
   Navegação
   ------------------------------------------------------------------------- */

function abrirVista(nome) {
  estado.vista = nome;
  document.querySelectorAll("[data-vista]").forEach((botao) => {
    botao.classList.toggle("ativo", botao.dataset.vista === nome);
  });
  ["painel", "contas", "transferir", "extrato"].forEach((vista) => {
    document.querySelector("#vista-" + vista).hidden = vista !== nome;
  });

  if (nome === "painel") carregarPainel();
  if (nome === "contas") carregarContas();
  if (nome === "transferir") carregarTransferencias();
  if (nome === "extrato") carregarExtrato();
}

/* -------------------------------------------------------------------------
   Painel
   ------------------------------------------------------------------------- */

async function carregarPainel() {
  try {
    const resumo = await dados.painel();
    el.cartaoSaldo.textContent = formatarMoeda(resumo.saldoTotal);
    el.cartaoAtivas.textContent = resumo.ativas;
    el.cartaoBloqueadas.textContent = resumo.bloqueadas;
    el.cartaoVolume.textContent = formatarMoeda(resumo.volumeMes);
    el.cartaoTransferencias.textContent = resumo.transferenciasMes;
    el.recursoTotal.textContent = resumo.totalContas;
    el.recursoAtivas.textContent = resumo.ativas;
    el.recursoBloqueadas.textContent = resumo.bloqueadas;
    el.recursoEncerradas.textContent = resumo.encerradas;

    el.alertaBloqueadas.hidden = resumo.bloqueadas === 0;
    el.textoBloqueadas.textContent = resumo.bloqueadas === 1
      ? "1 conta está bloqueada e não pode movimentar."
      : `${resumo.bloqueadas} contas estão bloqueadas e não podem movimentar.`;

    if (!resumo.ultimas.length) {
      el.corpoUltimas.innerHTML = `<tr><td colspan="4" class="vazio">Nenhuma movimentação registrada.</td></tr>`;
    } else {
      el.corpoUltimas.innerHTML = resumo.ultimas.map((mov) => `
        <tr>
          <td class="previsao">${formatarData(mov.criadaEm)}</td>
          <td><span class="conta-numero">${esc(mov.contaNumero)}</span></td>
          <td class="tipo-mov">${esc(mov.tipoRotulo)}</td>
          <td class="valor ${classeValor(mov.entrada)}">${esc(assinaturaValor(mov))}</td>
        </tr>
      `).join("");
    }
  } catch (falha) {
    mostrarErro(falha);
  }
}

/* -------------------------------------------------------------------------
   Contas
   ------------------------------------------------------------------------- */

function renderizarChipsStatus() {
  renderizarChips(el.chipsStatus, [
    { valor: "", rotulo: "Todas" },
    { valor: "ATIVA", rotulo: "Ativa" },
    { valor: "BLOQUEADA", rotulo: "Bloqueada" },
    { valor: "ENCERRADA", rotulo: "Encerrada" }
  ], estado.contas.status, "data-status");
}

async function carregarContas() {
  try {
    const resultado = await dados.listarContas({
      busca: estado.contas.busca,
      status: estado.contas.status,
      pagina: estado.contas.pagina,
      tamanho: TAMANHO_PAGINA
    });
    estado.contas.cache = resultado;
    renderizarContas(resultado);
  } catch (falha) {
    mostrarErro(falha);
  }
}

function renderizarContas(resultado) {
  if (!resultado) return;
  el.contagemContas.textContent = resultado.totalElements === 1
    ? "1 registro"
    : `${resultado.totalElements} registros`;

  if (!resultado.content.length) {
    el.corpoContas.innerHTML = `<tr><td colspan="7" class="vazio">Nenhuma conta encontrada.</td></tr>`;
    el.paginacaoContas.innerHTML = "";
    return;
  }

  el.corpoContas.innerHTML = resultado.content.map((conta) => {
    const confirmandoStatus = estado.confirmacoes.alternarConta === conta.id;
    const confirmandoEncerramento = estado.confirmacoes.encerrarConta === conta.id;
    const ativa = conta.status === "ATIVA";
    const bloqueada = conta.status === "BLOQUEADA";
    const encerrada = conta.status === "ENCERRADA";
    const acoes = [];

    acoes.push(`<button type="button" class="botao-acao" data-acao="extrato" data-id="${conta.id}">Extrato</button>`);
    if (ativa) {
      acoes.push(`<button type="button" class="botao-acao" data-acao="movimentar" data-id="${conta.id}">Movimentar</button>`);
    }
    if (!encerrada) {
      acoes.push(`<button type="button" class="botao-acao" data-acao="editar" data-id="${conta.id}">Editar</button>`);
    }
    if (ativa || bloqueada) {
      const rotulo = confirmandoStatus
        ? (ativa ? "Confirmar bloqueio" : "Confirmar desbloqueio")
        : (ativa ? "Bloquear" : "Desbloquear");
      acoes.push(`<button type="button" class="botao-acao ${confirmandoStatus ? "confirmando" : ""}" data-acao="alternar" data-id="${conta.id}">${rotulo}</button>`);
    }
    if (!encerrada) {
      const rotulo = confirmandoEncerramento ? "Confirmar encerramento" : "Encerrar";
      acoes.push(`<button type="button" class="botao-acao botao-perigo ${confirmandoEncerramento ? "confirmando" : ""}" data-acao="encerrar" data-id="${conta.id}">${rotulo}</button>`);
    }

    return `
      <tr>
        <td><span class="conta-numero">${esc(conta.numero)}</span></td>
        <td>
          <div class="conta-titular">
            <span>${esc(conta.titular)}</span>
            <small>${esc(conta.documento || "Sem documento")}</small>
          </div>
        </td>
        <td class="tipo-mov">${esc(conta.tipoRotulo)}</td>
        <td class="valor">${formatarMoeda(conta.saldo)}</td>
        <td class="valor valor-neutro">${formatarMoeda(conta.limite)}</td>
        <td><span class="status-pill ${classeStatus(conta.status)}">${esc(conta.statusRotulo)}</span></td>
        <td><div class="acoes">${acoes.join("")}</div></td>
      </tr>
    `;
  }).join("");

  renderizarPaginacao(el.paginacaoContas, Number(resultado.number) + 1, resultado.totalPages);
}

function abrirFormConta(conta = null) {
  estado.editandoContaId = conta ? conta.id : null;
  el.painelFormConta.hidden = false;
  el.tituloFormConta.textContent = conta ? `Editar conta ${conta.numero}` : "Nova conta";
  el.salvarConta.textContent = conta ? "Salvar alterações" : "Cadastrar conta";
  el.cancelarConta.hidden = !conta;
  el.contaNumero.disabled = Boolean(conta);
  el.formConta.reset();
  el.contaNumero.value = conta ? conta.numero : "";
  el.contaTitular.value = conta ? conta.titular : "";
  el.contaDocumento.value = conta ? (conta.documento || "") : "";
  el.contaTipo.value = conta ? conta.tipo : "CORRENTE";
  el.contaLimite.value = conta ? formatarNumero(conta.limite) : "";
  ajustarLimitePorTipo();
  el.painelFormConta.scrollIntoView({ behavior: "smooth", block: "nearest" });
  el.contaTitular.focus();
}

function fecharFormConta() {
  estado.editandoContaId = null;
  el.painelFormConta.hidden = true;
  el.formConta.reset();
  el.contaNumero.disabled = false;
  el.contaLimite.disabled = false;
  el.cancelarConta.hidden = true;
}

function ajustarLimitePorTipo() {
  const poupanca = el.contaTipo.value === "POUPANCA";
  el.contaLimite.disabled = poupanca;
  if (poupanca) el.contaLimite.value = "0,00";
  if (!poupanca && !el.contaLimite.value) el.contaLimite.value = "";
}

async function salvarConta(evento) {
  evento.preventDefault();
  const numeroConta = el.contaNumero.value.trim();
  const titular = el.contaTitular.value.trim();
  if (!numeroConta || !titular) {
    mostrarToast("Informe o número e o titular da conta.", "erro");
    return;
  }
  const limite = valorDe(el.contaLimite.value);
  const pedido = {
    numero: numeroConta,
    titular,
    documento: el.contaDocumento.value.trim() || null,
    tipo: el.contaTipo.value,
    limite: Number.isFinite(limite) && limite > 0 ? limite : 0
  };
  try {
    if (estado.editandoContaId) {
      await dados.atualizarConta(estado.editandoContaId, {
        titular: pedido.titular,
        documento: pedido.documento,
        tipo: pedido.tipo,
        limite: pedido.limite
      });
      mostrarToast("Conta atualizada.");
    } else {
      await dados.criarConta(pedido);
      mostrarToast(`Conta ${numeroConta} cadastrada.`);
    }
    fecharFormConta();
    await atualizarTudo();
  } catch (falha) {
    mostrarErro(falha);
  }
}

function abrirMovimento(conta) {
  estado.movendoContaId = conta.id;
  el.painelMovimento.hidden = false;
  el.tituloMovimento.textContent = `Movimentar — ${conta.numero}`;
  el.formMovimento.reset();
  el.movimentoTipo.value = "DEPOSITO";
  el.painelMovimento.scrollIntoView({ behavior: "smooth", block: "nearest" });
  el.movimentoValor.focus();
}

function fecharMovimento() {
  estado.movendoContaId = null;
  el.painelMovimento.hidden = true;
  el.formMovimento.reset();
}

async function salvarMovimento(evento) {
  evento.preventDefault();
  if (!estado.movendoContaId) return;
  const valor = valorDe(el.movimentoValor.value);
  if (!Number.isFinite(valor) || valor <= 0) {
    mostrarToast("Informe um valor maior que zero.", "erro");
    return;
  }
  const tipo = el.movimentoTipo.value;
  const descricao = el.movimentoDescricao.value.trim();
  try {
    if (tipo === "DEPOSITO") {
      await dados.depositar(estado.movendoContaId, valor, descricao);
      mostrarToast("Depósito realizado.");
    } else {
      await dados.sacar(estado.movendoContaId, valor, descricao);
      mostrarToast("Saque realizado.");
    }
    fecharMovimento();
    await atualizarTudo();
  } catch (falha) {
    mostrarErro(falha);
  }
}

async function alterarStatusConta(id, novoStatus) {
  try {
    await dados.alterarStatus(id, novoStatus);
    mostrarToast(novoStatus === "ENCERRADA"
      ? "Conta encerrada."
      : novoStatus === "BLOQUEADA" ? "Conta bloqueada." : "Conta desbloqueada.");
    await atualizarTudo();
  } catch (falha) {
    mostrarErro(falha);
  }
}

/* -------------------------------------------------------------------------
   Transferências
   ------------------------------------------------------------------------- */

async function carregarOpcoesContas() {
  try {
    const resultado = await dados.listarContas({ pagina: 1, tamanho: 50 });
    estado.todasContas = resultado.content;

    const ativas = estado.todasContas.filter((conta) => conta.status === "ATIVA");
    preencherSelect(el.transferenciaOrigem, ativas, "Selecione…");
    preencherSelect(el.transferenciaDestino, ativas, "Selecione…");

    const opcoesExtrato = estado.todasContas.map((conta) => ({
      valor: conta.id,
      rotulo: `${conta.numero} · ${conta.titular} (${conta.statusRotulo})`
    }));
    preencherSelectOpcoes(el.extratoConta, opcoesExtrato, "Selecione uma conta…", estado.extrato.contaId);
    atualizarDicaTransferencia();
  } catch (falha) {
    mostrarErro(falha);
  }
}

function preencherSelect(select, contas, vazio) {
  const selecionado = select.value;
  select.innerHTML = `<option value="">${esc(vazio)}</option>` + contas.map((conta) =>
    `<option value="${conta.id}">${esc(conta.numero)} · ${esc(conta.titular)} — ${esc(formatarMoeda(arredondar(conta.saldo + conta.limite)))} disponível</option>`
  ).join("");
  if ([...select.options].some((opcao) => opcao.value === selecionado)) {
    select.value = selecionado;
  }
}

function preencherSelectOpcoes(select, opcoes, vazio, selecionado) {
  select.innerHTML = `<option value="">${esc(vazio)}</option>` + opcoes.map((opcao) =>
    `<option value="${opcao.valor}">${esc(opcao.rotulo)}</option>`
  ).join("");
  if (selecionado && opcoes.some((opcao) => Number(opcao.valor) === Number(selecionado))) {
    select.value = String(selecionado);
  }
}

function atualizarDicaTransferencia() {
  const conta = estado.todasContas.find((item) => item.id === Number(el.transferenciaOrigem.value));
  if (!conta) {
    el.dicaTransferencia.textContent = "O saldo disponível inclui o limite da conta.";
    return;
  }
  el.dicaTransferencia.textContent = `Disponível em ${conta.numero}: ${formatarMoeda(arredondar(conta.saldo + conta.limite))} (saldo + limite).`;
}

async function carregarTransferencias() {
  await carregarOpcoesContas();
  await carregarTransferenciasRecentes();
}

async function carregarTransferenciasRecentes() {
  try {
    const recentes = await dados.recentes();
    if (!recentes.length) {
      el.corpoTransferencias.innerHTML = `<tr><td colspan="4" class="vazio">Nenhuma transferência registrada.</td></tr>`;
      return;
    }
    el.corpoTransferencias.innerHTML = recentes.map((mov) => `
      <tr>
        <td class="previsao">${formatarData(mov.criadaEm)}</td>
        <td><span class="conta-numero">${esc(mov.contaNumero)}</span></td>
        <td class="tipo-mov">${esc(mov.contraparte || "—")}</td>
        <td class="valor valor-negativo">${formatarMoeda(mov.valor)}</td>
      </tr>
    `).join("");
  } catch (falha) {
    mostrarErro(falha);
  }
}

async function salvarTransferencia(evento) {
  evento.preventDefault();
  const origemId = Number(el.transferenciaOrigem.value);
  const destinoId = Number(el.transferenciaDestino.value);
  if (!origemId || !destinoId) {
    mostrarToast("Selecione a conta de origem e a de destino.", "erro");
    return;
  }
  if (origemId === destinoId) {
    mostrarToast("A conta de origem deve ser diferente da conta de destino.", "erro");
    return;
  }
  const valor = valorDe(el.transferenciaValor.value);
  if (!Number.isFinite(valor) || valor <= 0) {
    mostrarToast("Informe um valor maior que zero.", "erro");
    return;
  }
  try {
    const recibo = await dados.transferir({
      origemId,
      destinoId,
      valor,
      descricao: el.transferenciaDescricao.value.trim()
    });
    el.reciboDetalhe.textContent = `${formatarMoeda(recibo.valor)} de ${recibo.origemNumero} (${recibo.origemTitular}) para ${recibo.destinoNumero} (${recibo.destinoTitular}).`;
    el.reciboSaldoOrigem.textContent = formatarMoeda(recibo.saldoOrigem);
    el.reciboSaldoDestino.textContent = formatarMoeda(recibo.saldoDestino);
    el.reciboTransferencia.hidden = false;
    el.formTransferencia.reset();
    mostrarToast("Transferência realizada.");
    await atualizarTudo();
  } catch (falha) {
    mostrarErro(falha);
  }
}

/* -------------------------------------------------------------------------
   Extrato
   ------------------------------------------------------------------------- */

function renderizarChipsExtrato() {
  renderizarChips(el.chipsExtrato, [
    { valor: "", rotulo: "Todas" },
    { valor: "DEPOSITO", rotulo: "Depósitos" },
    { valor: "SAQUE", rotulo: "Saques" },
    { valor: "TRANSFERENCIA_ENVIADA", rotulo: "Enviadas" },
    { valor: "TRANSFERENCIA_RECEBIDA", rotulo: "Recebidas" }
  ], estado.extrato.tipo, "data-tipo");
}

async function carregarExtrato() {
  await carregarOpcoesContas();
  if (!estado.todasContas.length) {
    el.corpoExtrato.innerHTML = `<tr><td colspan="6" class="vazio">Nenhuma conta cadastrada.</td></tr>`;
    return;
  }
  if (!estado.extrato.contaId || !estado.todasContas.some((conta) => conta.id === estado.extrato.contaId)) {
    estado.extrato.contaId = estado.todasContas[0].id;
  }
  el.extratoConta.value = String(estado.extrato.contaId);
  const conta = estado.todasContas.find((item) => item.id === estado.extrato.contaId);
  el.extratoTitulo.textContent = `${conta.numero} · ${conta.titular}`;
  el.extratoSubtitulo.textContent = `${conta.tipoRotulo} · movimentações mais recentes primeiro`;
  el.resumoExtrato.hidden = false;
  el.extratoSaldo.textContent = formatarMoeda(conta.saldo);
  el.extratoLimite.textContent = formatarMoeda(conta.limite);
  el.extratoStatus.textContent = conta.statusRotulo;
  el.extratoStatus.className = "status-pill " + classeStatus(conta.status);
  renderizarChipsExtrato();
  await carregarPaginaExtrato();
}

async function carregarPaginaExtrato() {
  if (!estado.extrato.contaId) return;
  try {
    const resultado = await dados.extrato(
      estado.extrato.contaId,
      estado.extrato.tipo,
      estado.extrato.pagina,
      TAMANHO_EXTRATO);
    if (!resultado.content.length) {
      el.corpoExtrato.innerHTML = `<tr><td colspan="6" class="vazio">Nenhuma movimentação neste filtro.</td></tr>`;
    } else {
      el.corpoExtrato.innerHTML = resultado.content.map((mov) => `
        <tr>
          <td class="previsao">${formatarData(mov.criadaEm)}</td>
          <td class="tipo-mov">${esc(mov.tipoRotulo)}</td>
          <td>${esc(mov.descricao || "—")}</td>
          <td class="tipo-mov">${esc(mov.contraparte || "—")}</td>
          <td class="valor ${classeValor(mov.entrada)}">${esc(assinaturaValor(mov))}</td>
          <td class="valor valor-neutro">${formatarMoeda(mov.saldoPosterior)}</td>
        </tr>
      `).join("");
    }
    renderizarPaginacao(el.paginacaoExtrato, Number(resultado.number) + 1, resultado.totalPages);
  } catch (falha) {
    mostrarErro(falha);
  }
}

async function abrirExtratoDaConta(contaId) {
  estado.extrato.contaId = contaId;
  estado.extrato.pagina = 1;
  abrirVista("extrato");
}

/* -------------------------------------------------------------------------
   Atualização geral e eventos
   ------------------------------------------------------------------------- */

async function atualizarTudo() {
  try {
    await carregarPainel();
    await carregarOpcoesContas();
    if (estado.vista === "contas") await carregarContas();
    if (estado.vista === "transferir") await carregarTransferenciasRecentes();
    if (estado.vista === "extrato") await carregarPaginaExtrato();
  } catch (falha) {
    mostrarErro(falha);
  }
}

async function detectarModo() {
  try {
    const resposta = await fetch("api/painel", { headers: { Accept: "application/json" } });
    if (!resposta.ok) throw new Error("API indisponível");
    estado.modo = "api";
    el.seloModo.textContent = "API conectada";
    el.seloModo.className = "selo api";
    el.avisoGeral.textContent = "";
  } catch (falha) {
    estado.modo = "demo";
    el.seloModo.textContent = "Modo demonstração";
    el.seloModo.className = "selo demo";
    el.avisoGeral.textContent = "Demonstração sem servidor: os dados ficam salvos apenas neste navegador.";
  }
}

function ligarEventos() {
  document.querySelectorAll("[data-vista]").forEach((botao) => {
    botao.addEventListener("click", () => abrirVista(botao.dataset.vista));
  });
  document.querySelectorAll("[data-ir]").forEach((botao) => {
    botao.addEventListener("click", () => abrirVista(botao.dataset.ir));
  });

  el.atualizarTudo.addEventListener("click", () => {
    atualizarTudo();
    mostrarToast("Dados atualizados.");
  });

  el.chipsStatus.addEventListener("click", (evento) => {
    const chip = evento.target.closest("[data-status]");
    if (!chip) return;
    estado.contas.status = chip.dataset.status;
    estado.contas.pagina = 1;
    renderizarChipsStatus();
    carregarContas();
  });

  el.buscaContas.addEventListener("input", () => {
    clearTimeout(estado.timerBuscaContas);
    estado.timerBuscaContas = setTimeout(() => {
      estado.contas.busca = el.buscaContas.value;
      estado.contas.pagina = 1;
      carregarContas();
    }, 320);
  });

  el.paginacaoContas.addEventListener("click", (evento) => {
    const botao = evento.target.closest("button[data-pagina]");
    if (!botao || botao.disabled) return;
    estado.contas.pagina = Number(botao.dataset.pagina);
    carregarContas();
  });

  el.botaoNovaConta.addEventListener("click", () => abrirFormConta());
  el.fecharFormConta.addEventListener("click", fecharFormConta);
  el.cancelarConta.addEventListener("click", fecharFormConta);
  el.formConta.addEventListener("submit", salvarConta);
  el.contaTipo.addEventListener("change", ajustarLimitePorTipo);

  el.fecharMovimento.addEventListener("click", fecharMovimento);
  el.cancelarMovimento.addEventListener("click", fecharMovimento);
  el.formMovimento.addEventListener("submit", salvarMovimento);

  el.corpoContas.addEventListener("click", (evento) => {
    const botao = evento.target.closest("button[data-acao]");
    if (!botao) return;
    const id = Number(botao.dataset.id);
    const conta = estado.contas.cache?.content.find((item) => item.id === id);
    if (!conta) return;
    const acao = botao.dataset.acao;

    if (acao === "extrato") {
      abrirExtratoDaConta(id);
    } else if (acao === "movimentar") {
      abrirMovimento(conta);
    } else if (acao === "editar") {
      abrirFormConta(conta);
    } else if (acao === "alternar") {
      const novo = conta.status === "ATIVA" ? "BLOQUEADA" : "ATIVA";
      duasEtapas("alternarConta", id, () => renderizarContas(estado.contas.cache), () =>
        alterarStatusConta(id, novo));
    } else if (acao === "encerrar") {
      duasEtapas("encerrarConta", id, () => renderizarContas(estado.contas.cache), () =>
        alterarStatusConta(id, "ENCERRADA"));
    }
  });

  el.transferenciaOrigem.addEventListener("change", atualizarDicaTransferencia);
  el.formTransferencia.addEventListener("submit", salvarTransferencia);

  el.extratoConta.addEventListener("change", () => {
    estado.extrato.contaId = Number(el.extratoConta.value) || null;
    estado.extrato.tipo = "";
    estado.extrato.pagina = 1;
    carregarExtrato();
  });

  el.chipsExtrato.addEventListener("click", (evento) => {
    const chip = evento.target.closest("[data-tipo]");
    if (!chip) return;
    estado.extrato.tipo = chip.dataset.tipo;
    estado.extrato.pagina = 1;
    renderizarChipsExtrato();
    carregarPaginaExtrato();
  });

  el.paginacaoExtrato.addEventListener("click", (evento) => {
    const botao = evento.target.closest("button[data-pagina]");
    if (!botao || botao.disabled) return;
    estado.extrato.pagina = Number(botao.dataset.pagina);
    carregarPaginaExtrato();
  });
}

async function iniciar() {
  renderizarChipsStatus();
  renderizarChipsExtrato();
  ligarEventos();
  await detectarModo();
  await atualizarTudo();
  abrirVista("painel");
}

iniciar();
